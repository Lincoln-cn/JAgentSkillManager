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
    @ConditionalOnProperty(prefix = "agent.skill", name = "folder-based-skills", havingValue = "true")
    public Object skillLifecycleInitializer(SkillLifecycleManager lifecycleManager,
                                            org.springframework.context.ApplicationContext applicationContext,
                                            AgentSkillManager agentSkillManager,
                                            FolderBasedSkillLoader folderBasedSkillLoader) {
        // This bean ensures the SkillLifecycleManager (a component) is initialized
        // by the application context. If auto-load is enabled, perform initialization
        // using configured skills directory.
        if (properties.isFolderBasedSkills() && properties.isAutoLoadSkills()) {
            try {
                String skillsDir = properties.getSkillsDirectory();
                logger.info("Initializing folder-based skills from directory: {}", skillsDir);
                lifecycleManager.initialize(Paths.get(skillsDir));
            } catch (Exception e) {
                logger.error("Failed to initialize skill lifecycle manager", e);
            }
        }

        // Auto-register any AgentSkill beans present in the application context
        try {
            if (properties.isAutoRegister()) {
                var beans = applicationContext.getBeansOfType(org.unreal.agent.skill.AgentSkill.class);
                beans.values().forEach(skill -> {
                    try {
                        agentSkillManager.registerSkill(skill);
                        logger.info("Auto-registered AgentSkill bean: {}", skill.getName());
                    } catch (Exception e) {
                        logger.warn("Failed to auto-register AgentSkill bean: {}", skill.getClass().getName(), e);
                    }
                });
            }
        } catch (Exception e) {
            logger.warn("Auto-register step failed", e);
        }

        // Also attempt to load any skills that declare a descriptor but have main pointing to
        // classes available as Spring beans (ensure FolderBasedSkillLoader can resolve them)
        try {
            // trigger a passive load to ensure loader's caches are ready (no-op if none)
            folderBasedSkillLoader.getLoadedSkills();
        } catch (Exception ignored) {}

        // Return a noop bean
        return new Object();
    }
    
    /**
     * Post-construction initialization.
     */
    @PostConstruct
    public void init() {
        logger.info("Agent Skill framework auto-configuration initialized");
        logger.info("Folder-based skills: {}", properties.isFolderBasedSkills());
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
