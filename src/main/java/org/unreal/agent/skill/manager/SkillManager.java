package org.unreal.agent.skill.manager;

import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for skill managers that handle different types of skills.
 */
public interface SkillManager {
    
    /**
     * Register a new skill.
     *
     * @param skill the skill to register
     */
    void registerSkill(AgentSkill skill);
    
    /**
     * Unregister a skill.
     *
     * @param skillName the name of the skill to unregister
     */
    void unregisterSkill(String skillName);
    
    /**
     * Get all registered skills.
     *
     * @return collection of all registered skills
     */
    Collection<AgentSkill> getAllSkills();
    
    /**
     * Get a skill by name.
     *
     * @param name the skill name
     * @return the skill if found, null otherwise
     */
    AgentSkill getSkill(String name);
    
    /**
     * Find a skill that can handle the given request.
     *
     * @param request the request to find a skill for
     * @return the skill that can handle the request, null if none found
     */
    AgentSkill findSkillForRequest(String request);
    
    /**
     * Execute a skill by name.
     *
     * @param skillName the name of the skill to execute
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
    AgentSkillResult executeSkill(String skillName, String request, Map<String, Object> parameters);
    
    /**
     * Execute a skill that can handle the given request.
     *
     * @param request the request to process
     * @param parameters the parameters for skill execution
     * @return the result of skill execution
     */
    AgentSkillResult executeSkill(String request, Map<String, Object> parameters);
}