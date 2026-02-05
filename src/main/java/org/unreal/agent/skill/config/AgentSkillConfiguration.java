package org.unreal.agent.skill.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Agent Skill framework.
 */
@Configuration
@ConditionalOnProperty(prefix = "agent.skill", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AgentSkillConfiguration {
}