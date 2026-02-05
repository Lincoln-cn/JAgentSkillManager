package org.unreal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.unreal.agent.skill.config.AgentSkillProperties;

/**
 * Agent Skill Manager Application
 * Provides agent skill management for Spring AI integration
 */
@SpringBootApplication
@EnableConfigurationProperties(AgentSkillProperties.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
