package org.unreal.agent.skill.folder.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Skill 资源定义
 */
public class SkillResources {
    
    @JsonProperty("scripts")
    private List<String> scripts;
    
    @JsonProperty("templates")
    private List<String> templates;
    
    @JsonProperty("configs")
    private List<String> configs;
    
    @JsonProperty("assets")
    private List<String> assets;
    
    public List<String> getScripts() {
        return scripts;
    }
    
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }
    
    public List<String> getTemplates() {
        return templates;
    }
    
    public void setTemplates(List<String> templates) {
        this.templates = templates;
    }
    
    public List<String> getConfigs() {
        return configs;
    }
    
    public void setConfigs(List<String> configs) {
        this.configs = configs;
    }
    
    public List<String> getAssets() {
        return assets;
    }
    
    public void setAssets(List<String> assets) {
        this.assets = assets;
    }
}