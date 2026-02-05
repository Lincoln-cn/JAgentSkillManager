package org.unreal.agent.skill;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing and executing agent skills.
 * This is the core component that coordinates skill execution and management.
 */
@Service
public class AgentSkillManager {
    
    private final Map<String, AgentSkill> skills = new ConcurrentHashMap<>();
    private final List<SkillExecutionListener> listeners = new ArrayList<>();
    
    /**
     * Register a new skill.
     * 
     * @param skill the skill to register
     */
    public void registerSkill(AgentSkill skill) {
        Objects.requireNonNull(skill, "Skill cannot be null");
        skills.put(skill.getName(), skill);
    }
    
    /**
     * Unregister a skill.
     * 
     * @param skillName the name of the skill to unregister
     */
    public void unregisterSkill(String skillName) {
        skills.remove(skillName);
    }
    
    /**
     * Get all registered skills.
     * 
     * @return collection of all registered skills
     */
    public Collection<AgentSkill> getAllSkills() {
        return new ArrayList<>(skills.values());
    }
    
    /**
     * Get a skill by name.
     * 
     * @param name the skill name
     * @return the skill if found, null otherwise
     */
    public AgentSkill getSkill(String name) {
        return skills.get(name);
    }
    
    /**
     * Find a skill that can handle the given request.
     * 
     * @param request the request to find a skill for
     * @return the skill that can handle the request, null if none found
     */
    public AgentSkill findSkillForRequest(String request) {
        return skills.values().stream()
                .filter(skill -> skill.canHandle(request))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Execute a skill by name.
     * 
     * @param skillName the name of the skill to execute
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
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
    
    /**
     * Execute a skill that can handle the given request.
     * 
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
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
        
        try {
            AgentSkillResult result = skill.execute(request, parameters);
            notifyExecutionCompleted(skill, request, parameters, result);
            return result;
        } catch (Exception e) {
            AgentSkillResult result = AgentSkillResult.failure()
                    .message("Skill execution failed: " + e.getMessage())
                    .skillName(skill.getName())
                    .metadata(Map.of("error", e.getClass().getSimpleName(), "request", request))
                    .build();
            notifyExecutionFailed(skill, request, parameters, e);
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