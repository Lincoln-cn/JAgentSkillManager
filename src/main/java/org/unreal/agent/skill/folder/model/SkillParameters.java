package org.unreal.agent.skill.folder.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Skill 参数定义
 */
public class SkillParameters {
    
    @JsonProperty("required")
    private Map<String, String> required;
    
    @JsonProperty("optional")
    private Map<String, String> optional;
    
    public Map<String, String> getRequired() {
        return required;
    }
    
    public void setRequired(Map<String, String> required) {
        this.required = required;
    }
    
    public Map<String, String> getOptional() {
        return optional;
    }
    
    public void setOptional(Map<String, String> optional) {
        this.optional = optional;
    }
}