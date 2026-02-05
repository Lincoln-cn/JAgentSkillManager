package org.unreal.agent.skill.dto;

import java.util.List;
import java.util.Map;

/**
 * Skill 验证结果 DTO
 */
public class SkillValidationDto {
    
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private String skillName;
    private Map<String, Object> metadata;
    
    public SkillValidationDto() {}
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}