package org.unreal.agent.skill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.folder.FolderBasedSkillLoader;
import org.unreal.agent.skill.folder.SkillLifecycleManager;
import org.unreal.agent.skill.springai.SpringAIAgentSkillAdapter;

import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;

/**
 * Auto-configuration for the Agent Skill framework.
 * This class provides Spring Boot auto-configuration for the agent skill management system.
 * Supports both traditional and agentskills.io format skills.
 */
@AutoConfiguration
@ComponentScan(basePackages = "org.unreal.agent.skill")
public class AgentSkillAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(AgentSkillAutoConfiguration.class);
    
    @Autowired
    private AgentSkillProperties properties;
    
    /**
     * Create the default AgentSkillManager bean.
     * 
     * @return AgentSkillManager instance
     */
    @Bean
    @ConditionalOnMissingBean
    public AgentSkillManager agentSkillManager() {
        return new AgentSkillManager();
    }
    
    /**
     * Create the SpringAIAgentSkillAdapter bean for Spring AI integration.
     * 
     * @param agentSkillManager the skill manager
     * @return SpringAIAgentSkillAdapter instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "agent.skill", name = "spring-ai-integration", havingValue = "true", matchIfMissing = true)
    public SpringAIAgentSkillAdapter springAIAgentSkillAdapter(AgentSkillManager agentSkillManager) {
        return new SpringAIAgentSkillAdapter(agentSkillManager);
    }
    
    /**
     * Create the FolderBasedSkillLoader bean for folder-based skill loading.
     * 
     * @return FolderBasedSkillLoader instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "agent.skill", name = "folder-based-skills", havingValue = "true")
    public FolderBasedSkillLoader folderBasedSkillLoader() {
        return new FolderBasedSkillLoader();
    }
    
    /**
     * Create the SkillLifecycleManager bean for dynamic skill management.
     * 
     * @param folderBasedSkillLoader the folder-based skill loader
     * @param agentSkillManager the skill manager
     * @return SkillLifecycleManager instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "agent.skill", name = "folder-based-skills", havingValue = "true")
    public SkillLifecycleManager skillLifecycleManager(
            FolderBasedSkillLoader folderBasedSkillLoader,
            AgentSkillManager agentSkillManager) {
        
        SkillLifecycleManager lifecycleManager = new SkillLifecycleManager();
        
        // Initialize lifecycle manager if folder-based skills are enabled
        if (properties.isFolderBasedSkills() && properties.isAutoLoadSkills()) {
            try {
                String skillsDir = properties.getSkillsDirectory();
                logger.info("Initializing folder-based skills from directory: {}", skillsDir);
                lifecycleManager.initialize(Paths.get(skillsDir));
            } catch (Exception e) {
                logger.error("Failed to initialize skill lifecycle manager", e);
            }
        }
        
        return lifecycleManager;
    }
    
    /**
     * Post-construction initialization.
     */
    @PostConstruct
    public void init() {
        logger.info("Agent Skill framework auto-configuration initialized");
        logger.info("Folder-based skills: {}", properties.isFolderBasedSkills());
        logger.info("Spring AI integration: {}", properties.isSpringAiIntegration());
        logger.info("Auto-load skills: {}", properties.isAutoLoadSkills());
        logger.info("agentskills.io support: {}", properties.isAgentskillsEnabled());
        
        if (properties.isFolderBasedSkills()) {
            logger.info("Skills directory: {}", properties.getSkillsDirectory());
            logger.info("Hot reload enabled: {}", properties.isHotReloadEnabled());
        }
        
        if (properties.isAgentskillsEnabled()) {
            logger.info("Strict validation: {}", properties.isStrictValidation());
            logger.info("Progressive disclosure: {}", properties.isProgressiveDisclosure());
            logger.info("Max SKILL.md size: {}KB", properties.getMaxSkillMdSizeKb());
        }
    }
}