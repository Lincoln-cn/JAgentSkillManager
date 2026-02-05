package org.unreal.agent.skill;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the result of executing an agent skill.
 */
public class AgentSkillResult {
    
    private final boolean success;
    private final String message;
    private final Object data;
    private final String skillName;
    private final LocalDateTime executionTime;
    private final Map<String, Object> metadata;
    
    private AgentSkillResult(Builder builder) {
        this.success = builder.success;
        this.message = builder.message;
        this.data = builder.data;
        this.skillName = builder.skillName;
        this.executionTime = LocalDateTime.now();
        this.metadata = builder.metadata;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    public String getSkillName() {
        return skillName;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public static Builder success() {
        return new Builder(true);
    }
    
    public static Builder failure() {
        return new Builder(false);
    }
    
    public static class Builder {
        private final boolean success;
        private String message;
        private Object data;
        private String skillName;
        private Map<String, Object> metadata;
        
        private Builder(boolean success) {
            this.success = success;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder data(Object data) {
            this.data = data;
            return this;
        }
        
        public Builder skillName(String skillName) {
            this.skillName = skillName;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public AgentSkillResult build() {
            return new AgentSkillResult(this);
        }
    }
}