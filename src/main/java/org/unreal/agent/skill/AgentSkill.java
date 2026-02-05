package org.unreal.agent.skill;

import java.util.Map;

/**
 * Core interface for agent skills in the Spring AI integration framework.
 * All agent skills should implement this interface to be compatible with the framework.
 */
public interface AgentSkill {
    
    /**
     * Get the unique name of this skill.
     * 
     * @return the skill name
     */
    String getName();
    
    /**
     * Get a description of what this skill does.
     * 
     * @return skill description
     */
    String getDescription();
    
    /**
     * Get the version of this skill.
     * 
     * @return skill version
     */
    String getVersion();
    
    /**
     * Check if this skill can handle the given request.
     * 
     * @param request the input request
     * @return true if this skill can handle the request
     */
    boolean canHandle(String request);
    
    /**
     * Execute the skill with the given request and parameters.
     * 
     * @param request the input request
     * @param parameters additional parameters for the skill execution
     * @return the result of skill execution
     */
    AgentSkillResult execute(String request, Map<String, Object> parameters);
    
    /**
     * Get the required parameters for this skill.
     * 
     * @return map of parameter names to their descriptions
     */
    Map<String, String> getRequiredParameters();
    
    /**
     * Get the optional parameters for this skill.
     * 
     * @return map of parameter names to their descriptions
     */
    Map<String, String> getOptionalParameters();

    /**
     * Get the instructions for this skill (Markdown content from SKILL.md).
     * 
     * @return the markdown instructions, or null if not applicable
     */
    default String getInstructions() {
        return null;
    }
}
