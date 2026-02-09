package org.unreal.agent.skill.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.unreal.agent.skill.service.SkillManagementService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * REST controller for skill management operations.
 * Provides endpoints for deploying, managing, and exporting agent skills.
 */
@RestController
@RequestMapping("/api/agent-skills/manage")
public class SkillManagementController {

    @Autowired
    private SkillManagementService managementService;

    /**
     * Deploy a skill from a ZIP file upload.
     *
     * @param file the ZIP file containing the skill
     * @param skillName optional skill name (if not provided, extracted from descriptor)
     * @return deployment result
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> deployFromZip(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "skillName", required = false) String skillName) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File is empty"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Only ZIP files are supported"));
        }

        SkillManagementService.SkillDeployResult result = managementService.deployFromZip(file, skillName);

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "skillName", result.getSkillName(),
                    "message", result.getMessage()
            ));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.getMessage()));
        }
    }

    /**
     * Delete a skill.
     *
     * @param skillName the name of the skill to delete
     * @return deletion result
     */
    @DeleteMapping("/{skillName}")
    public ResponseEntity<Map<String, Object>> deleteSkill(@PathVariable String skillName) {
        boolean deleted = managementService.deleteSkill(skillName);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Skill deleted successfully"
            ));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Failed to delete skill"));
        }
    }

    /**
     * Reload a skill from disk.
     *
     * @param skillName the name of the skill to reload
     * @return reload result
     */
    @PostMapping("/{skillName}/reload")
    public ResponseEntity<Map<String, Object>> reloadSkill(@PathVariable String skillName) {
        SkillManagementService.SkillDeployResult result = managementService.reloadSkill(skillName);

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "skillName", result.getSkillName(),
                    "message", result.getMessage()
            ));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", result.getMessage()));
        }
    }

    /**
     * Export a skill as a ZIP file.
     *
     * @param skillName the name of the skill to export
     * @return ZIP file download
     */
    @GetMapping("/{skillName}/export")
    public ResponseEntity<byte[]> exportSkill(@PathVariable String skillName) {
        try {
            byte[] zipContent = managementService.exportSkill(skillName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", skillName + ".zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipContent);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file tree of a skill.
     *
     * @param skillName the skill name
     * @return list of files and directories
     */
    @GetMapping("/{skillName}/files")
    public ResponseEntity<Map<String, Object>> getFileTree(@PathVariable String skillName) {
        List<SkillManagementService.FileInfo> files = managementService.getFileTree(skillName);

        if (files.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fileList = files.stream()
                .map(f -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("path", f.getPath());
                    map.put("isDirectory", f.isDirectory());
                    map.put("size", f.getSize());
                    map.put("lastModified", f.getLastModified());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "skillName", skillName,
                "files", fileList
        ));
    }

    /**
     * Read a file from a skill.
     *
     * @param skillName the skill name
     * @param filePath the file path (as path variable to support nested paths)
     * @return file content
     */
    @GetMapping("/{skillName}/files/{*filePath}")
    public ResponseEntity<Map<String, Object>> readFile(
            @PathVariable String skillName,
            @PathVariable String filePath) {

        // Remove leading slash if present
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        try {
            String content = managementService.readFile(skillName, filePath);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "skillName", skillName,
                    "path", filePath,
                    "content", content
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to read file: " + e.getMessage()));
        }
    }

    /**
     * Write/Update a file in a skill.
     *
     * @param skillName the skill name
     * @param filePath the file path
     * @param request request body containing content
     * @return result
     */
    @PutMapping("/{skillName}/files/{*filePath}")
    public ResponseEntity<Map<String, Object>> writeFile(
            @PathVariable String skillName,
            @PathVariable String filePath,
            @RequestBody Map<String, String> request) {

        // Remove leading slash if present
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        String content = request.get("content");
        if (content == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Content is required"));
        }

        try {
            managementService.writeFile(skillName, filePath, content);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File saved successfully",
                    "skillName", skillName,
                    "path", filePath
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to write file: " + e.getMessage()));
        }
    }

    /**
     * Delete a file from a skill.
     *
     * @param skillName the skill name
     * @param filePath the file path
     * @return result
     */
    @DeleteMapping("/{skillName}/files/{*filePath}")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @PathVariable String skillName,
            @PathVariable String filePath) {

        // Remove leading slash if present
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }

        try {
            managementService.deleteFile(skillName, filePath);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "File deleted successfully",
                    "skillName", skillName,
                    "path", filePath
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to delete file: " + e.getMessage()));
        }
    }
}
