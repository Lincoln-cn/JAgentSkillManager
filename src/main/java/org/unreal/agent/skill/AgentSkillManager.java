package org.unreal.agent.skill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;
import org.unreal.agent.skill.lifecycle.SkillEventManager;
import org.unreal.agent.skill.manager.SkillManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing and executing agent skills.
 * This is the core component that coordinates skill execution and management.
 * @deprecated Use implementations of {@link org.unreal.agent.skill.manager.SkillManager} instead
 */
@Service
public class AgentSkillManager implements SkillManager {
    
    private final Map<String, AgentSkill> skills = new ConcurrentHashMap<>();
    private final List<SkillExecutionListener> listeners = new ArrayList<>();
    
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
        notifyExecutionStarted(skill, request, parameters);
        long startTime = System.currentTimeMillis();

        try {
            AgentSkillResult result = skill.execute(request, parameters);
            long executionTime = System.currentTimeMillis() - startTime;
            
            notifyExecutionCompleted(skill, request, parameters, result);
            
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
                    .metadata(Map.of("error", e.getClass().getSimpleName(), "request", request))
                    .build();
            notifyExecutionFailed(skill, request, parameters, e);
            
            // Publish event even for failed executions
            if (eventManager != null) {
                eventManager.publishSkillExecuted(skill, request, parameters, result, executionTime);
            }
            
            return result;
        }
    }
    
    /**
     * Add a skill execution listener.
     * 
     * @param listener the listener to add
     */
    public void addListener(SkillExecutionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a skill execution listener.
     * 
     * @param listener the listener to remove
     */
    public void removeListener(SkillExecutionListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyExecutionStarted(AgentSkill skill, String request, Map<String, Object> parameters) {
        listeners.forEach(listener -> {
            try {
                listener.onExecutionStarted(skill, request, parameters);
            } catch (Exception e) {
                // Log error but don't propagate
            }
        });
    }
    
    private void notifyExecutionCompleted(AgentSkill skill, String request, Map<String, Object> parameters, AgentSkillResult result) {
        listeners.forEach(listener -> {
            try {
                listener.onExecutionCompleted(skill, request, parameters, result);
            } catch (Exception e) {
                // Log error but don't propagate
            }
        });
    }
    
    private void notifyExecutionFailed(AgentSkill skill, String request, Map<String, Object> parameters, Exception error) {
        listeners.forEach(listener -> {
            try {
                listener.onExecutionFailed(skill, request, parameters, error);
            } catch (Exception e) {
                // Log error but don't propagate
            }
        });
    }
    
    /**
     * Interface for listening to skill execution events.
     */
    public interface SkillExecutionListener {
        default void onExecutionStarted(AgentSkill skill, String request, Map<String, Object> parameters) {}
        default void onExecutionCompleted(AgentSkill skill, String request, Map<String, Object> parameters, AgentSkillResult result) {}
        default void onExecutionFailed(AgentSkill skill, String request, Map<String, Object> parameters, Exception error) {}
    }
}