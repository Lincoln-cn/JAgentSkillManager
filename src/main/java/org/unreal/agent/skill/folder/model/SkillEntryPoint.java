package org.unreal.agent.skill.folder.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Skill 入口点定义
 */
public class SkillEntryPoint {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("method")
    private String method = "execute";
    
    @JsonProperty("keywords")
    private List<String> keywords;
    
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}