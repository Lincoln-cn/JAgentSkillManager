package org.unreal.agent.skill.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Skill 执行结果 DTO
 */
public class SkillExecuteResultDto {
    
    private boolean success;
    private String message;
    private Object data;
    private String skillName;
    private LocalDateTime executionTime;
    private Map<String, Object> metadata;
    
    public SkillExecuteResultDto() {}
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}