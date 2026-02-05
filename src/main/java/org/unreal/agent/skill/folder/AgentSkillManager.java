package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for managing skills following agentskills.io specification.
 * Provides validation, metadata extraction, and skill organization utilities.
 */
@Component("folderAgentSkillManager")
public class AgentSkillManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentSkillManager.class);
    
    /**
     * Validate a skill against agentskills.io specification.
     * 
     * @param skillPath path to the skill directory
     * @return ValidationResult with errors and warnings
     */
    public ValidationResult validateSkill(Path skillPath) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Check if SKILL.md exists
            Path skillMd = skillPath.resolve("SKILL.md");
            if (!Files.exists(skillMd)) {
                errors.add("SKILL.md file not found (required by agentskills.io spec)");
                return new ValidationResult(false, errors, warnings);
            }
            
            // Parse SKILL.md and validate frontmatter
            SkillDescriptor descriptor = SkillMarkdownParser.parse(skillMd);
            
            // Validate required fields
            if (descriptor.getName() == null || descriptor.getName().trim().isEmpty()) {
                errors.add("name field is required in SKILL.md frontmatter");
            } else {
                // Validate name format: lowercase, numbers, hyphens only
                if (!isValidSkillName(descriptor.getName())) {
                    errors.add("Invalid skill name: must be lowercase letters, numbers, and hyphens only");
                }
                
                // Check if name matches folder name
                String folderName = skillPath.getFileName().toString();
                if (!descriptor.getName().equals(folderName)) {
                    errors.add("Skill name in frontmatter must match folder name: " + 
                              descriptor.getName() + " != " + folderName);
                }
            }
            
            if (descriptor.getDescription() == null || descriptor.getDescription().trim().isEmpty()) {
                errors.add("description field is required in SKILL.md frontmatter");
            } else if (descriptor.getDescription().length() > 1024) {
                errors.add("description exceeds 1024 character limit");
            }
            
            // Validate optional fields if present
            if (descriptor.getCompatibility() != null && descriptor.getCompatibility().length() > 500) {
                errors.add("compatibility field exceeds 500 character limit");
            }
            
            // Check for recommended structure
            validateRecommendedStructure(skillPath, warnings);
            
            // Check file sizes
            validateFileSize(skillMd, warnings);
            
        } catch (Exception e) {
            errors.add("Failed to parse SKILL.md: " + e.getMessage());
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    /**
     * Validate skill name according to agentskills.io specification.
     * 
     * @param name skill name to validate
     * @return true if valid
     */
    private boolean isValidSkillName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Check length (1-64 characters)
        if (name.length() < 1 || name.length() > 64) {
            return false;
        }
        
        // Cannot start or end with hyphen
        if (name.startsWith("-") || name.endsWith("-")) {
            return false;
        }
        
        // Cannot contain consecutive hyphens
        if (name.contains("--")) {
            return false;
        }
        
        // Only lowercase letters, numbers, and hyphens
        return name.matches("^[a-z0-9-]+$");
    }
    
    /**
     * Validate recommended directory structure.
     * 
     * @param skillPath path to the skill
     * @param warnings list to add warnings to
     */
    private void validateRecommendedStructure(Path skillPath, List<String> warnings) {
        // Check for optional directories
        String[] recommendedDirs = {"scripts", "references", "assets"};
        for (String dir : recommendedDirs) {
            Path dirPath = skillPath.resolve(dir);
            if (!Files.exists(dirPath)) {
                warnings.add("Recommended directory not found: " + dir);
            }
        }
    }
    
    /**
     * Validate file sizes for efficient context usage.
     * 
     * @param file file to check
     * @param warnings list to add warnings to
     */
    private void validateFileSize(Path file, List<String> warnings) {
        try {
            long fileSizeBytes = Files.size(file);
            long fileSizeKB = fileSizeBytes / 1024;
            
            // Check SKILL.md size (recommended < 5000 tokens, approximately < 20KB)
            if (fileSizeKB > 20) {
                warnings.add("SKILL.md file is large (" + fileSizeKB + "KB), consider splitting content into reference files");
            }
        } catch (IOException e) {
            logger.warn("Could not check file size: " + file, e);
        }
    }
    
    /**
     * Get skill metadata for progressive disclosure.
     * 
     * @param skillPath path to skill directory
     * @return SkillMetadata with discovery and activation info
     */
    public SkillMetadata getSkillMetadata(Path skillPath) {
        try {
            Path skillMd = skillPath.resolve("SKILL.md");
            if (!Files.exists(skillMd)) {
                return null;
            }
            
            SkillDescriptor descriptor = SkillMarkdownParser.parse(skillMd);
            
            // Discovery metadata (name, description only)
            String discoveryInfo = String.format("%s: %s", 
                descriptor.getName(), 
                truncateDescription(descriptor.getDescription()));
            
            // Full metadata including additional fields
            Map<String, Object> fullMetadata = new HashMap<>();
            fullMetadata.put("name", descriptor.getName());
            fullMetadata.put("description", descriptor.getDescription());
            fullMetadata.put("version", descriptor.getVersion());
            fullMetadata.put("author", descriptor.getAuthor());
            fullMetadata.put("license", descriptor.getLicense());
            fullMetadata.put("compatibility", descriptor.getCompatibility());
            fullMetadata.put("keywords", descriptor.getKeywords());
            fullMetadata.put("allowed_tools", descriptor.getAllowedTools());
            fullMetadata.put("metadata", descriptor.getExtraMetadata());
            
            // File information
            fullMetadata.put("file_size", getFileSize(skillMd));
            fullMetadata.put("last_modified", getFileModified(skillMd));
            
            // Structure information
            fullMetadata.put("has_scripts", Files.exists(skillPath.resolve("scripts")));
            fullMetadata.put("has_references", Files.exists(skillPath.resolve("references")));
            fullMetadata.put("has_assets", Files.exists(skillPath.resolve("assets")));
            
            return new SkillMetadata(discoveryInfo, fullMetadata, descriptor.getInstructions());
            
        } catch (Exception e) {
            logger.error("Failed to get skill metadata: " + skillPath, e);
            return null;
        }
    }
    
    /**
     * Truncate description for discovery purposes.
     * 
     * @param description full description
     * @return truncated description (~100 tokens)
     */
    private String truncateDescription(String description) {
        if (description.length() <= 150) {
            return description;
        }
        return description.substring(0, 147) + "...";
    }
    
    /**
     * Get file size in human readable format.
     */
    private String getFileSize(Path file) {
        try {
            long bytes = Files.size(file);
            return formatFileSize(bytes);
        } catch (IOException e) {
            return "unknown";
        }
    }
    
    /**
     * Get file last modified time.
     */
    private String getFileModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toString();
        } catch (IOException e) {
            return "unknown";
        }
    }
    
    /**
     * Format file size in human readable format.
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return (bytes / (1024 * 1024)) + " MB";
        } else {
            return (bytes / (1024 * 1024 * 1024)) + " GB";
        }
    }
    
    /**
     * List all skills in a directory with their metadata.
     * 
     * @param skillsDir directory containing skills
     * @return list of SkillMetadata objects
     */
    public List<SkillMetadata> listSkillsInDirectory(Path skillsDir) {
        if (!Files.exists(skillsDir) || !Files.isDirectory(skillsDir)) {
            logger.warn("Skills directory does not exist: {}", skillsDir);
            return Collections.emptyList();
        }
        
        return Arrays.stream(skillsDir.toFile().listFiles())
                .filter(File::isDirectory)
                .map(File::toPath)
                .map(this::getSkillMetadata)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(meta -> meta.getName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Search for skills by keywords in their descriptions.
     * 
     * @param skillsDir directory containing skills
     * @param keywords keywords to search for
     * @return list of matching skills
     */
    public List<SkillMetadata> searchSkillsByKeywords(Path skillsDir, List<String> keywords) {
        List<SkillMetadata> allSkills = listSkillsInDirectory(skillsDir);
        
        if (keywords == null || keywords.isEmpty()) {
            return allSkills;
        }
        
        List<String> lowerKeywords = keywords.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        return allSkills.stream()
                .filter(skill -> {
                    String description = skill.getDiscoveryInfo().toLowerCase();
                    String name = skill.getName().toLowerCase();
                    
                    return lowerKeywords.stream()
                            .anyMatch(keyword -> 
                                description.contains(keyword) || name.contains(keyword));
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Generate skill documentation in agentskills.io format.
     * 
     * @param skillPath path to existing skill
     * @param outputPath where to generate documentation
     * @throws IOException if generation fails
     */
    public void generateSkillDocumentation(Path skillPath, Path outputPath) throws IOException {
        SkillMetadata metadata = getSkillMetadata(skillPath);
        if (metadata == null) {
            throw new IOException("Failed to load skill metadata");
        }
        
        StringBuilder doc = new StringBuilder();
        
        // Generate documentation structure
        doc.append("# ").append(metadata.getName()).append("\n\n");
        doc.append(metadata.getDiscoveryInfo()).append("\n\n");
        
        // Add usage examples
        doc.append("## Usage Examples\n\n");
        doc.append("```json\n");
        doc.append("{\n");
        doc.append("  \"skill\": \"").append(metadata.getName()).append("\",\n");
        doc.append("  \"request\": \"Your request here\"\n");
        doc.append("}\n");
        doc.append("```\n\n");
        
        // Add parameter information
        Map<String, Object> fullMetadata = metadata.getFullMetadata();
        if (fullMetadata.containsKey("keywords") && fullMetadata.get("keywords") != null) {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) fullMetadata.get("keywords");
            if (!keywords.isEmpty()) {
                doc.append("## Keywords: ").append(String.join(", ", keywords)).append("\n\n");
            }
        }
        
        Files.write(outputPath, doc.toString().getBytes(StandardCharsets.UTF_8));
        logger.info("Generated skill documentation: {}", outputPath);
    }
    
    /**
     * Skill metadata container for agentskills.io format.
     */
    public static class SkillMetadata {
        private final String name;
        private final String discoveryInfo;
        private final Map<String, Object> fullMetadata;
        private final String instructions;
        
        public SkillMetadata(String discoveryInfo, Map<String, Object> fullMetadata, String instructions) {
            this.discoveryInfo = discoveryInfo;
            this.fullMetadata = fullMetadata;
            this.instructions = instructions;
            this.name = (String) fullMetadata.get("name");
        }
        
        public String getName() {
            return name;
        }
        
        public String getDiscoveryInfo() {
            return discoveryInfo;
        }
        
        public Map<String, Object> getFullMetadata() {
            return fullMetadata;
        }
        
        public String getInstructions() {
            return instructions;
        }
    }
    
    /**
     * Validation result container.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}