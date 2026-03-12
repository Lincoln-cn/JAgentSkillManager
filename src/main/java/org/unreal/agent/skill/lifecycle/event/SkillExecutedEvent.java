package org.unreal.agent.skill.lifecycle.event;

import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;

import java.util.Map;

/**
 * Event published when a skill is executed.
 */
public class SkillExecutedEvent extends SkillEvent {
    
    private final String request;
    private final Map<String, Object> parameters;
    private final AgentSkillResult result;
    private final long executionTime;
    
    public SkillExecutedEvent(Object source, AgentSkill skill, String request, 
                             Map<String, Object> parameters, AgentSkillResult result, long executionTime) {
        super(source, skill);
        this.request = request;
        this.parameters = parameters;
        this.result = result;
        this.executionTime = executionTime;
    }
    
    public String getRequest() {
        return request;
    }
    
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    public AgentSkillResult getResult() {
        return result;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
}