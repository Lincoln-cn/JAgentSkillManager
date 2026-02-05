package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.vo.SkillMetadataVo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Skill 元数据服务
 */
@Service
public class SkillMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(SkillMetadataService.class);

    @Autowired
    private AgentSkillManager skillManager;

    public SkillMetadataVo getMetadata(Path skillPath) {
        try {
            Path skillMd = skillPath.resolve("SKILL.md");
            if (!Files.exists(skillMd)) {
                return null;
            }

            SkillDescriptor descriptor = SkillMarkdownParser.parse(skillMd);
            String discoveryInfo = String.format("%s: %s",
                    descriptor.getName(),
                    truncateDescription(descriptor.getDescription()));

            Map<String, Object> fullMetadata = buildFullMetadata(descriptor, skillMd, skillPath);

            return new SkillMetadataVo(discoveryInfo, fullMetadata, descriptor.getInstructions());

        } catch (Exception e) {
            logger.error("Failed to get skill metadata: {}", skillPath, e);
            return null;
        }
    }

    public List<SkillMetadataVo> listSkills(Path skillsDir) {
        if (!Files.exists(skillsDir) || !Files.isDirectory(skillsDir)) {
            logger.warn("Skills directory does not exist: {}", skillsDir);
            return Collections.emptyList();
        }

        return java.util.Arrays.stream(skillsDir.toFile().listFiles())
                .filter(java.io.File::isDirectory)
                .map(java.io.File::toPath)
                .map(this::getMetadata)
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(SkillMetadataVo::getName))
                .collect(Collectors.toList());
    }

    public List<SkillMetadataVo> searchByKeywords(Path skillsDir, List<String> keywords) {
        List<SkillMetadataVo> allSkills = listSkills(skillsDir);

        if (keywords == null || keywords.isEmpty()) {
            return allSkills;
        }

        String lowerKeywords = keywords.stream()
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));

        return allSkills.stream()
                .filter(skill -> {
                    String description = skill.getDiscoveryInfo().toLowerCase();
                    String name = skill.getName().toLowerCase();
                    return java.util.Arrays.stream(lowerKeywords.split("\\s+"))
                            .anyMatch(keyword ->
                                    description.contains(keyword) || name.contains(keyword));
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getActivationInfo(String skillName) {
        AgentSkill skill = skillManager.getSkill(skillName);
        if (skill == null) {
            return null;
        }

        Map<String, Object> info = new ConcurrentHashMap<>();
        info.put("name", skill.getName());
        info.put("description", skill.getDescription());
        info.put("version", skill.getVersion());
        info.put("required_parameters", skill.getRequiredParameters());
        info.put("optional_parameters", skill.getOptionalParameters());
        info.put("instructions", skill.getInstructions());

        return info;
    }

    private Map<String, Object> buildFullMetadata(SkillDescriptor descriptor, Path skillMd, Path skillPath) {
        Map<String, Object> fullMetadata = new ConcurrentHashMap<>();
        fullMetadata.put("name", descriptor.getName());
        fullMetadata.put("description", descriptor.getDescription());
        fullMetadata.put("version", descriptor.getVersion());
        fullMetadata.put("author", descriptor.getAuthor());
        fullMetadata.put("license", descriptor.getLicense());
        fullMetadata.put("compatibility", descriptor.getCompatibility());
        fullMetadata.put("keywords", descriptor.getKeywords());
        fullMetadata.put("allowed_tools", descriptor.getAllowedTools());
        fullMetadata.put("extra_metadata", descriptor.getExtraMetadata());
        fullMetadata.put("file_size", getFileSize(skillMd));
        fullMetadata.put("last_modified", getFileModified(skillMd));
        fullMetadata.put("has_scripts", Files.exists(skillPath.resolve("scripts")));
        fullMetadata.put("has_references", Files.exists(skillPath.resolve("references")));
        fullMetadata.put("has_assets", Files.exists(skillPath.resolve("assets")));
        fullMetadata.put("has_examples", Files.exists(skillPath.resolve("examples")));
        return fullMetadata;
    }

    private String truncateDescription(String description) {
        if (description.length() <= 150) {
            return description;
        }
        return description.substring(0, 147) + "...";
    }

    private String getFileSize(Path file) {
        try {
            long bytes = Files.size(file);
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
            return (bytes / (1024 * 1024)) + " MB";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private String getFileModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toString();
        } catch (IOException e) {
            return "unknown";
        }
    }
}