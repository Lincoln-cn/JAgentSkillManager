package org.unreal.agent.skill.vo;

/**
 * Skill 发现信息 Value Object
 */
public class SkillDiscoveryVo {
    
    private String name;
    private String description;
    private String version;
    
    public SkillDiscoveryVo() {}
    
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
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
}