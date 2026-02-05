package org.unreal.agent.skill.vo;

import java.util.Map;

/**
 * Skill 函数包装器 Value Object
 */
public class SkillFunctionVo {
    
    private String name;
    private String description;
    private Map<String, Object> parameters;
    
    public SkillFunctionVo() {}
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}