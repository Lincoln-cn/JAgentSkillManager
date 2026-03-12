package org.unreal.agent.skill.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Unified skill manager that delegates to specialized skill managers based on skill type.
 */
@Component
public class UnifiedSkillManager implements SkillManager {
    
    @Autowired
    private List<SkillManager> skillManagers;
    
    @Override
    public void registerSkill(AgentSkill skill) {
        // Default to the first available manager
        if (!skillManagers.isEmpty()) {
            skillManagers.get(0).registerSkill(skill);
        }
    }
    
    @Override
    public void unregisterSkill(String skillName) {
        // Try to unregister from all managers
        for (SkillManager manager : skillManagers) {
            try {
                manager.unregisterSkill(skillName);
            } catch (Exception e) {
                // Continue to next manager
            }
        }
    }
    
    @Override
    public Collection<AgentSkill> getAllSkills() {
        // Combine skills from all managers
        return skillManagers.stream()
                .flatMap(manager -> manager.getAllSkills().stream())
                .distinct()
                .toList();
    }
    
    @Override
    public AgentSkill getSkill(String name) {
        // Try to get the skill from each manager
        for (SkillManager manager : skillManagers) {
            AgentSkill skill = manager.getSkill(name);
            if (skill != null) {
                return skill;
            }
        }
        return null;
    }
    
    @Override
    public AgentSkill findSkillForRequest(String request) {
        // Try to find a skill in each manager
        for (SkillManager manager : skillManagers) {
            AgentSkill skill = manager.findSkillForRequest(request);
            if (skill != null) {
                return skill;
            }
        }
        return null;
    }
    
    @Override
    public AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters) {
        // Try to execute the skill using each manager
        for (SkillManager manager : skillManagers) {
            AgentSkill skill = manager.getSkill(skillName);
            if (skill != null) {
                return manager.executeSkill(skillName, request, parameters);
            }
        }
        
        return AgentSkillResult.failure()
                .message("Skill not found in any manager: " + skillName)
                .skillName(skillName)
                .build();
    }
    
    @Override
    public AgentSkillResult executeSkill(String request, Map<String, Object> parameters) {
        // Try to find and execute a skill in each manager
        for (SkillManager manager : skillManagers) {
            AgentSkill skill = manager.findSkillForRequest(request);
            if (skill != null) {
                return manager.executeSkill(request, parameters);
            }
        }
        
        return AgentSkillResult.failure()
                .message("No skill found to handle request: " + request)
                .build();
    }
}