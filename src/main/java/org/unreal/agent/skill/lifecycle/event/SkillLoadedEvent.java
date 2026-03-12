package org.unreal.agent.skill.lifecycle.event;

import org.unreal.agent.skill.core.AgentSkill;

/**
 * Event published when a skill is loaded.
 */
public class SkillLoadedEvent extends SkillEvent {
    
    public SkillLoadedEvent(Object source, AgentSkill skill) {
        super(source, skill);
    }
}