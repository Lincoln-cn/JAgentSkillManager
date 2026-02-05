package org.unreal.agent.skill.dto;

import java.util.List;
import java.util.Map;

/**
 * Skill 元数据 DTO
 */
public class SkillMetadataDto {
    
    private String name;
    private String description;
    private String version;
    private String author;
    private List<String> tags;
    private List<String> keywords;
    private String category;
    private String license;
    private boolean enabled;
    private Map<String, Object> extraMetadata;
    
    public SkillMetadataDto() {}
    
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
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getLicense() {
        return license;
    }
    
    public void setLicense(String license) {
        this.license = license;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }
    
    public void setExtraMetadata(Map<String, Object> extraMetadata) {
        this.extraMetadata = extraMetadata;
    }
}