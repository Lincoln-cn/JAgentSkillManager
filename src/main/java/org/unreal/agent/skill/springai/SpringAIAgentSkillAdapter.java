package org.unreal.agent.skill.springai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.AgentSkillResult;

import java.util.*;

/**
 * Spring AI integration adapter that provides integration utilities for AgentSkills with Spring AI.
 * Enhanced to support agentskills.io specification and progressive disclosure.
 * This component acts as a bridge between the skill framework and Spring AI.
 */
@Component
public class SpringAIAgentSkillAdapter {
    
    private final AgentSkillManager skillManager;
    private final Map<String, SkillFunctionWrapper> functionWrappers = new HashMap<>();
    
    @Autowired
    public SpringAIAgentSkillAdapter(AgentSkillManager skillManager) {
        this.skillManager = skillManager;
        initializeFunctionWrappers();
    }
    
    /**
     * Initialize function wrappers for all registered skills.
     */
    private void initializeFunctionWrappers() {
        skillManager.getAllSkills().forEach(this::registerSkillFunction);
        
        // Listen for new skill registrations
        skillManager.addListener(new AgentSkillManager.SkillExecutionListener() {
            @Override
            public void onExecutionStarted(AgentSkill skill, String request, Map<String, Object> parameters) {
                // Register new skills as they become available
                if (!functionWrappers.containsKey(skill.getName())) {
                    registerSkillFunction(skill);
                }
            }
        });
    }
    
    /**
     * Get skill discovery information (name and description only).
     * 
     * @return List of skill discovery information strings
     */
    public List<String> getSkillDiscoveryInfo() {
        List<String> discoveryInfo = new ArrayList<>();
        for (SkillFunctionWrapper wrapper : functionWrappers.values()) {
            AgentSkill skill = wrapper.getSkill();
            discoveryInfo.add(skill.getName() + ": " + skill.getDescription());
        }
        return discoveryInfo;
    }
    
    /**
     * Get all skill information structured for agentskills.io integration.
     * 
     * @return Map containing all skill information organized by tier
     */
    public Map<String, Object> getAllSkillsForAgentskillsIo() {
        Map<String, Object> result = new HashMap<>();
        
        List<String> discoveryInfo = getSkillDiscoveryInfo();
        Map<String, Object> activationInfo = new HashMap<>();
        
        for (String skillName : functionWrappers.keySet()) {
            SkillFunctionWrapper wrapper = functionWrappers.get(skillName);
            AgentSkill skill = wrapper.getSkill();
            Map<String, Object> skillInfo = new HashMap<>();
            skillInfo.put("name", skill.getName());
            skillInfo.put("description", skill.getDescription());
            skillInfo.put("version", skill.getVersion());
            skillInfo.put("required_parameters", skill.getRequiredParameters());
            skillInfo.put("optional_parameters", skill.getOptionalParameters());
            skillInfo.put("instructions", skill.getInstructions());
            activationInfo.put(skillName, skillInfo);
        }
        
        result.put("discovery", discoveryInfo);
        result.put("activation", activationInfo);
        
        return result;
    }
    
    /**
     * Register a skill as a function wrapper.
     * 
     * @param skill the skill to register
     */
    public void registerSkillFunction(AgentSkill skill) {
        SkillFunctionWrapper wrapper = new SkillFunctionWrapper(skill, skillManager);
        functionWrappers.put(skill.getName(), wrapper);
    }
    
    /**
     * Unregister a skill function wrapper.
     * 
     * @param skillName the name of the skill to unregister
     */
    public void unregisterSkillFunction(String skillName) {
        functionWrappers.remove(skillName);
    }
    
    /**
     * Get all function wrappers for registered skills.
     * 
     * @return map of function name to SkillFunctionWrapper
     */
    public Map<String, SkillFunctionWrapper> getFunctionWrappers() {
        return new HashMap<>(functionWrappers);
    }
    
    /**
     * Get function definitions for Spring AI integration.
     * 
     * @return list of function definitions
     */
    public List<Map<String, Object>> getFunctionDefinitions() {
        List<Map<String, Object>> definitions = new ArrayList<>();
        
        for (SkillFunctionWrapper wrapper : functionWrappers.values()) {
            Map<String, Object> definition = new HashMap<>();
            definition.put("name", wrapper.getSkill().getName());
            definition.put("description", wrapper.getSkill().getDescription());
            definition.put("parameters", createParameterSchema(wrapper.getSkill()));
            definitions.add(definition);
        }
        
        return definitions;
    }
    
    /**
     * Get all instruction-based content from skills for system prompt augmentation.
     * 
     * @return concatenated instructions from all skills
     */
    public String getAllInstructions() {
        StringBuilder sb = new StringBuilder();
        sb.append("You have access to the following skills with detailed instructions:\n\n");
        
        for (SkillFunctionWrapper wrapper : functionWrappers.values()) {
            String instructions = wrapper.getSkill().getInstructions();
            if (instructions != null && !instructions.trim().isEmpty()) {
                sb.append("### Skill: ").append(wrapper.getSkill().getName()).append("\n");
                sb.append(instructions).append("\n\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Execute a skill function by name.
     * 
     * @param functionName the name of the skill function
     * @param arguments the arguments for the function
     * @return the result of function execution
     */
    public Object executeFunction(String functionName, Map<String, Object> arguments) {
        SkillFunctionWrapper wrapper = functionWrappers.get(functionName);
        if (wrapper == null) {
            throw new IllegalArgumentException("Unknown function: " + functionName);
        }
        
        return wrapper.execute(arguments);
    }
    
    /**
     * Create parameter schema for Spring AI function calling.
     * 
     * @param skill the skill
     * @return parameter schema
     */
    private Map<String, Object> createParameterSchema(AgentSkill skill) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        
        Map<String, String> requiredParams = skill.getRequiredParameters();
        Map<String, String> optionalParams = skill.getOptionalParameters();
        
        // Add request parameter
        Map<String, Object> requestProperty = new HashMap<>();
        requestProperty.put("type", "string");
        requestProperty.put("description", "The request text to process");
        properties.put("request", requestProperty);
        required.add("request");
        
        // Add required parameters
        requiredParams.forEach((name, desc) -> {
            Map<String, Object> property = new HashMap<>();
            property.put("type", "string");
            property.put("description", desc);
            properties.put(name, property);
            required.add(name);
        });
        
        // Add optional parameters
        optionalParams.forEach((name, desc) -> {
            Map<String, Object> property = new HashMap<>();
            property.put("type", "string");
            property.put("description", desc);
            properties.put(name, property);
        });
        
        schema.put("properties", properties);
        schema.put("required", required);
        
        return schema;
    }
    
    /**
     * Wrapper class that encapsulates a skill for function calling.
     */
    public static class SkillFunctionWrapper {
        private final AgentSkill skill;
        private final AgentSkillManager skillManager;
        
        public SkillFunctionWrapper(AgentSkill skill, AgentSkillManager skillManager) {
            this.skill = skill;
            this.skillManager = skillManager;
        }
        
        public AgentSkill getSkill() {
            return skill;
        }
        
        public Object execute(Map<String, Object> arguments) {
            String request = (String) arguments.get("request");
            if (request == null) {
                request = "";
            }
            
            // Remove request from arguments as it's passed separately
            Map<String, Object> parameters = new HashMap<>(arguments);
            parameters.remove("request");
            
            AgentSkillResult result = skillManager.executeSkill(skill.getName(), request, parameters);
            
            if (result.isSuccess()) {
                return result.getData() != null ? result.getData() : result.getMessage();
            } else {
                throw new RuntimeException("Skill execution failed: " + result.getMessage());
            }
        }
    }
}