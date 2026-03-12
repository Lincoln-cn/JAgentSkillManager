package org.unreal.agent.skill.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;
import org.unreal.agent.skill.lifecycle.SkillEventManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of SkillManager that manages skills in memory.
 */
@Service
public class DefaultSkillManager implements SkillManager {
    
    private final Map<String, AgentSkill> skills = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private SkillEventManager eventManager;
    
    @Override
    public void registerSkill(AgentSkill skill) {
        Objects.requireNonNull(skill, "Skill cannot be null");
        skills.put(skill.getName(), skill);
        
        // Publish event if event manager is available
        if (eventManager != null) {
            eventManager.publishSkillLoaded(skill);
        }
    }

    @Override
    public void unregisterSkill(String skillName) {
        AgentSkill removedSkill = skills.remove(skillName);
        
        // Publish event if event manager is available and skill was removed
        if (eventManager != null && removedSkill != null) {
            eventManager.publishSkillUnloaded(removedSkill);
        }
    }
    
    @Override
    public Collection<AgentSkill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }
    
    @Override
    public AgentSkill getSkill(String name) {
        return skills.get(name);
    }
    
    @Override
    public AgentSkill findSkillForRequest(String request) {
        return skills.values().stream()
                .filter(skill -> skill.canHandle(request))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters) {
        AgentSkill skill = skills.get(skillName);
        if (skill == null) {
            return AgentSkillResult.failure()
                    .message("Skill not found: " + skillName)
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
                    .message("No skill found to handle request: " + request)
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
        long startTime = System.currentTimeMillis();
        try {
            AgentSkillResult result = skill.execute(request, parameters);
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Publish event if event manager is available
            if (eventManager != null) {
                eventManager.publishSkillExecuted(skill, request, parameters, result, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            AgentSkillResult result = AgentSkillResult.failure()
                    .message("Skill execution failed: " + e.getMessage())
                    .skillName(skill.getName())
                    .build();
            
            // Publish event even for failed executions
            if (eventManager != null) {
                eventManager.publishSkillExecuted(skill, request, parameters, result, executionTime);
            }
            
            return result;
        }
    }
}