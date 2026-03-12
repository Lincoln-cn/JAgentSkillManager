package org.unreal.agent.skill.lifecycle.event;

import org.springframework.context.ApplicationEvent;
import org.unreal.agent.skill.core.AgentSkill;

/**
 * Base class for skill-related events.
 */
public abstract class SkillEvent extends ApplicationEvent {
    
    private final AgentSkill skill;
    private final String skillName;
    
    public SkillEvent(Object source, AgentSkill skill) {
        super(source);
        this.skill = skill;
        this.skillName = skill != null ? skill.getName() : "unknown";
    }
    
    public AgentSkill getSkill() {
        return skill;
    }
    
    public String getSkillName() {
        return skillName;
    }
}