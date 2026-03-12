package org.unreal.agent.skill.lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.core.AgentSkill;
import org.unreal.agent.skill.core.AgentSkillResult;
import org.unreal.agent.skill.lifecycle.event.SkillExecutedEvent;
import org.unreal.agent.skill.lifecycle.event.SkillLoadedEvent;
import org.unreal.agent.skill.lifecycle.event.SkillUnloadedEvent;

import java.util.Map;

/**
 * Component responsible for publishing skill-related events.
 */
@Component
public class SkillEventManager {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Publishes a skill loaded event.
     * 
     * @param skill the loaded skill
     */
    public void publishSkillLoaded(AgentSkill skill) {
        eventPublisher.publishEvent(new SkillLoadedEvent(this, skill));
    }
    
    /**
     * Publishes a skill unloaded event.
     * 
     * @param skill the unloaded skill
     */
    public void publishSkillUnloaded(AgentSkill skill) {
        eventPublisher.publishEvent(new SkillUnloadedEvent(this, skill));
    }
    
    /**
     * Publishes a skill executed event.
     * 
     * @param skill the executed skill
     * @param request the request that was processed
     * @param parameters the parameters used
     * @param result the execution result
     * @param executionTime the execution time in milliseconds
     */
    public void publishSkillExecuted(AgentSkill skill, String request, 
                                   Map<String, Object> parameters, 
                                   AgentSkillResult result, long executionTime) {
        eventPublisher.publishEvent(new SkillExecutedEvent(this, skill, request, parameters, result, executionTime));
    }
}