package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.config.AgentSkillProperties;
import org.unreal.agent.skill.dto.SkillValidationResult;
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
 * Manager for agentskills.io specification compliance and advanced features.
 * Handles progressive disclosure, validation, and integration with third-party Spring AI services.
 */
@Component
public class AgentskillsManager {

    private static final Logger logger = LoggerFactory.getLogger(AgentskillsManager.class);

    @Autowired
    private AgentSkillManager skillManager;

    @Autowired
    private AgentSkillProperties properties;

    @Autowired
    private FolderBasedSkillLoader folderBasedSkillLoader;

    @Autowired
    private SkillValidator skillValidator;

    @Autowired
    private SkillMetadataService metadataService;

    private final Map<String, SkillDescriptor> skillDescriptors = new ConcurrentHashMap<>();
    private final Map<String, SkillValidationResult> validationResults = new ConcurrentHashMap<>();

    public SkillValidationResult validateSkill(Path skillPath) {
        return skillValidator.validate(skillPath);
    }

    public SkillMetadataVo getSkillMetadata(Path skillPath) {
        return metadataService.getMetadata(skillPath);
    }

    public List<SkillMetadataVo> listSkillsInDirectory(Path skillsDir) {
        return metadataService.listSkills(skillsDir);
    }

    public List<SkillMetadataVo> searchSkillsByKeywords(Path skillsDir, List<String> keywords) {
        return metadataService.searchByKeywords(skillsDir, keywords);
    }

    public List<String> getSkillDiscoveryInfo() {
        return skillManager.getAllSkills().stream()
                .map(skill -> skill.getName() + ": " + skill.getDescription())
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSkillActivationInfo(String skillName) {
        return metadataService.getActivationInfo(skillName);
    }
}