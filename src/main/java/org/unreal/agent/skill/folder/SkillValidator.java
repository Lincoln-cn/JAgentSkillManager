package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.dto.SkillValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Skill 验证服务
 */
@Component
public class SkillValidator {

    private static final Logger logger = LoggerFactory.getLogger(SkillValidator.class);

    public SkillValidationResult validate(Path skillPath) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            Path skillMd = skillPath.resolve("SKILL.md");
            if (!Files.exists(skillMd)) {
                errors.add("SKILL.md file not found");
                return new SkillValidationResult(false, errors, warnings);
            }

            SkillDescriptor descriptor = SkillMarkdownParser.parse(skillMd);
            validateDescriptor(descriptor, skillPath, errors, warnings);
            validateFileSize(skillMd, warnings);

        } catch (Exception e) {
            errors.add("Failed to parse SKILL.md: " + e.getMessage());
        }

        return new SkillValidationResult(errors.isEmpty(), errors, warnings);
    }

    private void validateDescriptor(SkillDescriptor descriptor, Path skillPath, 
                                   List<String> errors, List<String> warnings) {
        if (descriptor.getName() == null || descriptor.getName().trim().isEmpty()) {
            errors.add("name field is required");
        } else {
            if (!isValidSkillName(descriptor.getName())) {
                errors.add("Invalid skill name format");
            }
            String folderName = skillPath.getFileName().toString();
            if (!descriptor.getName().equals(folderName)) {
                errors.add("Skill name must match folder name");
            }
        }

        if (descriptor.getDescription() == null || descriptor.getDescription().trim().isEmpty()) {
            errors.add("description field is required");
        }
    }

    private boolean isValidSkillName(String name) {
        if (name == null || name.isEmpty() || name.length() > 64) {
            return false;
        }
        if (name.startsWith("-") || name.endsWith("-") || name.contains("--")) {
            return false;
        }
        return name.matches("^[a-z0-9-]+$");
    }

    private void validateFileSize(Path file, List<String> warnings) {
        try {
            long sizeKB = Files.size(file) / 1024;
            if (sizeKB > 20) {
                warnings.add("SKILL.md is large (" + sizeKB + "KB)");
            }
        } catch (IOException e) {
            logger.warn("Could not check file size: {}", file);
        }
    }
}