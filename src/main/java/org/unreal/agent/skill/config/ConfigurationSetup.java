package org.unreal.agent.skill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

/**
 * Configuration class for the Agent Skill framework configuration.
 */
@Configuration
@EnableConfigurationProperties(AgentSkillProperties.class)
public class ConfigurationSetup {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSetup.class);
    
    @Autowired
    private AgentSkillProperties properties;
    
    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }
    
    @Bean
    public ConfigurationValidator configurationValidator() {
        return new ConfigurationValidator();
    }
    
    /**
     * Logs the configuration values at startup for debugging purposes.
     */
    public void logConfigurationValues() {
        logger.info("Agent Skill Framework Configuration:");
        logger.info("  Enabled: {}", properties.isEnabled());
        logger.info("  Auto Register: {}", properties.isAutoRegister());
        logger.info("  Execution Timeout: {}ms", properties.getExecutionTimeout());
        logger.info("  Max Concurrent Executions: {}", properties.getMaxConcurrentExecutions());
        logger.info("  Metrics Enabled: {}", properties.isMetricsEnabled());
        logger.info("  Folder-Based Skills: {}", properties.isFolderBasedSkills());
        logger.info("  Skills Directory: {}", properties.getSkillsDirectory());
        logger.info("  Hot Reload Enabled: {}", properties.isHotReloadEnabled());
        logger.info("  Auto Load Skills: {}", properties.isAutoLoadSkills());
        logger.info("  Watch Polling Interval: {}ms", properties.getWatchPollingInterval());
        logger.info("  Validate Skills On Load: {}", properties.isValidateSkillsOnLoad());
        logger.info("  Descriptor Patterns: {}", String.join(", ", properties.getDescriptorPatterns()));
        logger.info("  agentskills.io Enabled: {}", properties.isAgentskillsEnabled());
        logger.info("  Strict Validation: {}", properties.isStrictValidation());
        logger.info("  Max SKILL.md Size: {}KB", properties.getMaxSkillMdSizeKb());
        logger.info("  Progressive Disclosure: {}", properties.isProgressiveDisclosure());
        logger.info("  Enable Metadata Cache: {}", properties.isEnableMetadataCache());
        logger.info("  Cache Expiration: {}ms", properties.getCacheExpirationMs());
        logger.info("  Max Cache Size: {}", properties.getMaxCacheSize());
        logger.info("  Enable Cache Stats: {}", properties.isEnableCacheStats());
    }
}