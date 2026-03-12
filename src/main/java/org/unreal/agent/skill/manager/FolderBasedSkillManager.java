package org.unreal.agent.skill.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;

import java.util.Collection;
import java.util.Map;

/**
 * Skill manager specifically for folder-based skills.
 */
@Component
public class FolderBasedSkillManager implements SkillManager {
    
    @Autowired
    private FolderBasedSkillLoader skillLoader;
    
    @Override
    public void registerSkill(AgentSkill skill) {
        // Folder-based skills are loaded from the file system
        throw new UnsupportedOperationException("Folder-based skills are loaded from file system");
    }
    
    @Override
    public void unregisterSkill(String skillName) {
        // Folder-based skills are managed by the skill loader
        skillLoader.unloadSkill(skillName);
    }
    
    @Override
    public Collection<AgentSkill> getAllSkills() {
        return skillLoader.getLoadedSkills().values().stream()
                .map(FolderBasedSkillLoader.LoadedSkill::getSkillInstance)
                .toList();
    }
    
    @Override
    public AgentSkill getSkill(String name) {
        FolderBasedSkillLoader.LoadedSkill loadedSkill = skillLoader.getLoadedSkill(name);
        return loadedSkill != null ? loadedSkill.getSkillInstance() : null;
    }
    
    @Override
    public AgentSkill findSkillForRequest(String request) {
        return getAllSkills().stream()
                .filter(skill -> skill.canHandle(request))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters) {
        AgentSkill skill = getSkill(skillName);
        if (skill == null) {
            return AgentSkillResult.failure()
                    .message("Folder-based skill not found: " + skillName)
                    .skillName(skillName)
                    .build();
        }
        
        return executeSkill(skill, request, parameters);
    }
    
    @Override
    public AgentSkillResult executeSkill(String request, Map<String, Object> parameters) {
        AgentSkill skill = findSkillForRequest(request);
        if (skill == null) {
            return AgentSkillResult.failure()
                    .message("No folder-based skill found to handle request: " + request)
                    .build();
        }
        
        return executeSkill(skill, request, parameters);
    }
    
    /**
     * Execute a specific skill instance.
     *
     * @param skill the skill to execute
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
    private AgentSkillResult executeSkill(AgentSkill skill, String request, Map<String, Object> parameters) {
        try {
            return skill.execute(request, parameters);
        } catch (Exception e) {
            return AgentSkillResult.failure()
                    .message("Folder-based skill execution failed: " + e.getMessage())
                    .skillName(skill.getName())
                    .build();
        }
    }
}