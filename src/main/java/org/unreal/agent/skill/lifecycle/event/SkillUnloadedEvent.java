package org.unreal.agent.skill.lifecycle.event;

import org.unreal.agent.skill.core.AgentSkill;

/**
 * Event published when a skill is unloaded.
 */
public class SkillUnloadedEvent extends SkillEvent {
    
    public SkillUnloadedEvent(Object source, AgentSkill skill) {
        super(source, skill);
    }
}