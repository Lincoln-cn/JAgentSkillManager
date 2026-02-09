package org.unreal.agent.skill.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service for managing skill lifecycle operations including upload, deployment,
 * file management, and export.
 */
@Service
public class SkillManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SkillManagementService.class);

    @Autowired
    private AgentSkillManager agentSkillManager;

    @Autowired
    private FolderBasedSkillLoader skillLoader;

    @Autowired
    private org.unreal.agent.skill.config.AgentSkillProperties skillProperties;

    /**
     * Upload and deploy a skill from a ZIP file.
     *
     * @param file the ZIP file containing the skill
     * @param skillName optional skill name (if null, will be extracted from descriptor)
     * @return deployment result
     */
    public SkillDeployResult deployFromZip(MultipartFile file, String skillName) {
        try {
            // Create temp directory for extraction
            Path tempDir = Files.createTempDirectory("skill-upload-");
            Path zipFile = tempDir.resolve("skill.zip");
            file.transferTo(zipFile);

            // Extract ZIP
            Path extractDir = tempDir.resolve("extracted");
            Files.createDirectories(extractDir);
            extractZip(zipFile, extractDir);

            // Find the skill root folder (may be nested inside the ZIP)
            Path skillRoot = findSkillRoot(extractDir);
            if (skillRoot == null) {
                return SkillDeployResult.failure("Cannot find skill descriptor (skill.json, skill.yaml, or SKILL.md) in uploaded file");
            }

            // Load skill descriptor to get the actual name
            FolderBasedSkillLoader.LoadedSkill tempSkill = skillLoader.loadSkillFromFolder(skillRoot);
            if (tempSkill == null) {
                return SkillDeployResult.failure("Failed to load skill from uploaded file. Please check the skill descriptor format.");
            }

            String actualSkillName = tempSkill.getDescriptor().getName();
            if (skillName != null && !skillName.isEmpty() && !skillName.equals(actualSkillName)) {
                // Rename skill folder if explicit name is provided
                actualSkillName = skillName;
            }

            // Check if skill already exists
            if (agentSkillManager.getSkill(actualSkillName) != null) {
                // Unload existing skill
                skillLoader.unloadSkill(actualSkillName);
                agentSkillManager.unregisterSkill(actualSkillName);
            }

            // Move to skills directory
            Path skillsDir = getSkillsDirectory();
            Path targetDir = skillsDir.resolve(actualSkillName);

            // Remove existing directory if present
            if (Files.exists(targetDir)) {
                deleteDirectory(targetDir);
            }

            // Move extracted skill to target location
            moveDirectory(skillRoot, targetDir);

            // Load the skill
            FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.loadSkillFromFolder(targetDir);
            if (loadedSkill != null) {
                agentSkillManager.registerSkill(loadedSkill.getSkillInstance());

                // Cleanup temp directory
                deleteDirectory(tempDir);

                return SkillDeployResult.success(actualSkillName, "Skill deployed successfully");
            } else {
                return SkillDeployResult.failure("Failed to load skill after deployment");
            }

        } catch (Exception e) {
            logger.error("Failed to deploy skill from ZIP", e);
            return SkillDeployResult.failure("Deployment failed: " + e.getMessage());
        }
    }

    /**
     * Delete a skill and its files.
     *
     * @param skillName the name of the skill to delete
     * @return true if deleted successfully
     */
    public boolean deleteSkill(String skillName) {
        try {
            // Unload from memory
            FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
            if (loadedSkill != null) {
                skillLoader.unloadSkill(skillName);
                agentSkillManager.unregisterSkill(skillName);
            }

            // Delete files
            Path skillsDir = getSkillsDirectory();
            Path skillDir = skillsDir.resolve(skillName);
            if (Files.exists(skillDir)) {
                deleteDirectory(skillDir);
            }

            logger.info("Deleted skill: {}", skillName);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete skill: {}", skillName, e);
            return false;
        }
    }

    /**
     * Reload a skill from disk.
     *
     * @param skillName the name of the skill to reload
     * @return reload result
     */
    public SkillDeployResult reloadSkill(String skillName) {
        try {
            FolderBasedSkillLoader.LoadedSkill existingSkill = skillLoader.getLoadedSkill(skillName);
            if (existingSkill == null) {
                return SkillDeployResult.failure("Skill not found: " + skillName);
            }

            Path skillFolder = existingSkill.getSkillFolder();

            // Unload existing
            skillLoader.unloadSkill(skillName);
            agentSkillManager.unregisterSkill(skillName);

            // Reload
            FolderBasedSkillLoader.LoadedSkill reloadedSkill = skillLoader.loadSkillFromFolder(skillFolder);
            if (reloadedSkill != null) {
                agentSkillManager.registerSkill(reloadedSkill.getSkillInstance());
                return SkillDeployResult.success(skillName, "Skill reloaded successfully");
            } else {
                return SkillDeployResult.failure("Failed to reload skill");
            }
        } catch (Exception e) {
            logger.error("Failed to reload skill: {}", skillName, e);
            return SkillDeployResult.failure("Reload failed: " + e.getMessage());
        }
    }

    /**
     * Export a skill to a ZIP file.
     *
     * @param skillName the name of the skill to export
     * @return the ZIP file as byte array
     */
    public byte[] exportSkill(String skillName) throws IOException {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zipDirectory(skillFolder, skillFolder, zos);
        }

        return baos.toByteArray();
    }

    /**
     * Get file tree of a skill.
     *
     * @param skillName the skill name
     * @return list of file info
     */
    public List<FileInfo> getFileTree(String skillName) {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            return Collections.emptyList();
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        List<FileInfo> files = new ArrayList<>();

        try {
            Files.walk(skillFolder).forEach(path -> {
                if (!path.equals(skillFolder)) {
                    String relativePath = skillFolder.relativize(path).toString();
                    files.add(new FileInfo(
                        relativePath,
                        Files.isDirectory(path),
                        getFileSize(path),
                        getLastModified(path)
                    ));
                }
            });
        } catch (IOException e) {
            logger.error("Failed to get file tree for skill: {}", skillName, e);
        }

        return files;
    }

    /**
     * Read file content.
     *
     * @param skillName the skill name
     * @param filePath the file path relative to skill folder
     * @return file content as string
     */
    public String readFile(String skillName, String filePath) throws IOException {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        Path targetFile = skillFolder.resolve(filePath).normalize();

        // Security check: ensure the file is within skill folder
        if (!targetFile.startsWith(skillFolder)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }

        if (!Files.exists(targetFile) || Files.isDirectory(targetFile)) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        return Files.readString(targetFile);
    }

    /**
     * Write file content.
     *
     * @param skillName the skill name
     * @param filePath the file path relative to skill folder
     * @param content the content to write
     */
    public void writeFile(String skillName, String filePath, String content) throws IOException {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        Path targetFile = skillFolder.resolve(filePath).normalize();

        // Security check
        if (!targetFile.startsWith(skillFolder)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }

        // Create parent directories if needed
        Files.createDirectories(targetFile.getParent());

        // Write file
        Files.writeString(targetFile, content);

        logger.info("Updated file: {} in skill: {}", filePath, skillName);
    }

    /**
     * Delete a file from skill.
     *
     * @param skillName the skill name
     * @param filePath the file path relative to skill folder
     */
    public void deleteFile(String skillName, String filePath) throws IOException {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(skillName);
        if (loadedSkill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        Path skillFolder = loadedSkill.getSkillFolder();
        Path targetFile = skillFolder.resolve(filePath).normalize();

        // Security check
        if (!targetFile.startsWith(skillFolder)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }

        if (Files.exists(targetFile)) {
            if (Files.isDirectory(targetFile)) {
                deleteDirectory(targetFile);
            } else {
                Files.delete(targetFile);
            }
            logger.info("Deleted file: {} from skill: {}", filePath, skillName);
        }
    }

    // Helper methods

    private Path getSkillsDirectory() {
        String skillsDir = skillProperties.getSkillsDirectory();
        if (skillsDir == null || skillsDir.isEmpty()) {
            skillsDir = "skills";
        }
        return Paths.get(skillsDir).toAbsolutePath().normalize();
    }

    private void extractZip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();

                // Security check: ensure entry is within target directory
                if (!entryPath.startsWith(targetDir)) {
                    logger.warn("Skipping suspicious ZIP entry: {}", entry.getName());
                    continue;
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private Path findSkillRoot(Path extractDir) throws IOException {
        // Check if skill descriptor is directly in extractDir
        if (hasSkillDescriptor(extractDir)) {
            return extractDir;
        }

        // Check subdirectories (handle case where ZIP contains a parent folder)
        try (var stream = Files.list(extractDir)) {
            Optional<Path> subDir = stream
                .filter(Files::isDirectory)
                .filter(this::hasSkillDescriptor)
                .findFirst();

            if (subDir.isPresent()) {
                return subDir.get();
            }
        }

        return null;
    }

    private boolean hasSkillDescriptor(Path dir) {
        return Files.exists(dir.resolve("skill.json"))
            || Files.exists(dir.resolve("skill.yaml"))
            || Files.exists(dir.resolve("skill.yml"))
            || Files.exists(dir.resolve("SKILL.md"));
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;

        Files.walk(dir)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    logger.error("Failed to delete: {}", path, e);
                }
            });
    }

    private void moveDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path relativePath = source.relativize(sourcePath);
                Path targetPath = target.resolve(relativePath);

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to move file: " + sourcePath, e);
            }
        });

        // Delete source after copy
        deleteDirectory(source);
    }

    private void zipDirectory(Path sourceDir, Path currentDir, ZipOutputStream zos) throws IOException {
        try (var stream = Files.list(currentDir)) {
            for (Path path : stream.toList()) {
                String zipEntryName = sourceDir.relativize(path).toString();

                if (Files.isDirectory(path)) {
                    zipDirectory(sourceDir, path, zos);
                } else {
                    ZipEntry entry = new ZipEntry(zipEntryName);
                    zos.putNextEntry(entry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            }
        }
    }

    private long getFileSize(Path path) {
        try {
            return Files.isDirectory(path) ? 0 : Files.size(path);
        } catch (IOException e) {
            return 0;
        }
    }

    private long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }

    // Result classes

    public static class SkillDeployResult {
        private final boolean success;
        private final String skillName;
        private final String message;

        private SkillDeployResult(boolean success, String skillName, String message) {
            this.success = success;
            this.skillName = skillName;
            this.message = message;
        }

        public static SkillDeployResult success(String skillName, String message) {
            return new SkillDeployResult(true, skillName, message);
        }

        public static SkillDeployResult failure(String message) {
            return new SkillDeployResult(false, null, message);
        }

        public boolean isSuccess() { return success; }
        public String getSkillName() { return skillName; }
        public String getMessage() { return message; }
    }

    public static class FileInfo {
        private final String path;
        private final boolean isDirectory;
        private final long size;
        private final long lastModified;

        public FileInfo(String path, boolean isDirectory, long size, long lastModified) {
            this.path = path;
            this.isDirectory = isDirectory;
            this.size = size;
            this.lastModified = lastModified;
        }

        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
    }
}
