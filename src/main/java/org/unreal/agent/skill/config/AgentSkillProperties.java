package org.unreal.agent.skill.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for configuring the Agent Skill framework.
 */
@ConfigurationProperties(prefix = "agent.skill")
public class AgentSkillProperties {
    
    /**
     * Whether the agent skill framework is enabled.
     */
    private boolean enabled = true;
    
    /**
     * Whether to automatically register skills found in the classpath.
     */
    private boolean autoRegister = true;

    /**
     * Default timeout for skill execution in milliseconds.
     */
    private long executionTimeout = 30000;
    
    /**
     * Maximum number of concurrent skill executions.
     */
    private int maxConcurrentExecutions = 10;
    
    /**
     * Whether to enable skill execution metrics.
     */
    private boolean metricsEnabled = false;
    
    // Folder-based skill properties
    
    /**
     * Whether to enable folder-based skill loading.
     */
    private boolean folderBasedSkills = false;
    
    /**
     * Directory path for folder-based skills.
     */
    private String skillsDirectory = "skills";
    
    /**
     * Whether to enable hot reload for folder-based skills.
     */
    private boolean hotReloadEnabled = true;
    
    /**
     * Whether to automatically load skills from the skills directory on startup.
     */
    private boolean autoLoadSkills = true;
    
    /**
     * File watch polling interval in milliseconds.
     */
    private long watchPollingInterval = 1000;
    
    /**
     * Whether to validate skills on load.
     */
    private boolean validateSkillsOnLoad = true;
    
    /**
     * Supported skill descriptor file patterns.
     */
    private String[] descriptorPatterns = {"skill.json", "skill.yaml", "skill.yml"};
    
    // agentskills.io specific properties
    
    /**
     * Whether to enable agentskills.io specification support.
     */
    private boolean agentskillsEnabled = false;
    
    /**
     * Whether to perform strict validation of agentskills.io compliance.
     */
    private boolean strictValidation = false;
    
    /**
     * Maximum size of SKILL.md file in kilobytes.
     */
    private long maxSkillMdSizeKb = 20;
    
    /**
     * Whether to enable progressive disclosure as per agentskills.io spec.
     */
    private boolean progressiveDisclosure = true;
    
    /**
     * Whether to enable metadata caching for performance.
     */
    private boolean enableMetadataCache = true;
    
    // Getters and setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public long getExecutionTimeout() {
        return executionTimeout;
    }
    
    public void setExecutionTimeout(long executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
    
    public int getMaxConcurrentExecutions() {
        return maxConcurrentExecutions;
    }
    
    public void setMaxConcurrentExecutions(int maxConcurrentExecutions) {
        this.maxConcurrentExecutions = maxConcurrentExecutions;
    }
    
    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }
    
    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }
    
    public boolean isFolderBasedSkills() {
        return folderBasedSkills;
    }
    
    public void setFolderBasedSkills(boolean folderBasedSkills) {
        this.folderBasedSkills = folderBasedSkills;
    }
    
    public String getSkillsDirectory() {
        return skillsDirectory;
    }
    
    public void setSkillsDirectory(String skillsDirectory) {
        this.skillsDirectory = skillsDirectory;
    }
    
    public boolean isHotReloadEnabled() {
        return hotReloadEnabled;
    }
    
    public void setHotReloadEnabled(boolean hotReloadEnabled) {
        this.hotReloadEnabled = hotReloadEnabled;
    }
    
    public boolean isAutoLoadSkills() {
        return autoLoadSkills;
    }
    
    public void setAutoLoadSkills(boolean autoLoadSkills) {
        this.autoLoadSkills = autoLoadSkills;
    }
    
    public long getWatchPollingInterval() {
        return watchPollingInterval;
    }
    
    public void setWatchPollingInterval(long watchPollingInterval) {
        this.watchPollingInterval = watchPollingInterval;
    }
    
    public boolean isValidateSkillsOnLoad() {
        return validateSkillsOnLoad;
    }
    
    public void setValidateSkillsOnLoad(boolean validateSkillsOnLoad) {
        this.validateSkillsOnLoad = validateSkillsOnLoad;
    }
    
    public String[] getDescriptorPatterns() {
        return descriptorPatterns;
    }
    
    public void setDescriptorPatterns(String[] descriptorPatterns) {
        this.descriptorPatterns = descriptorPatterns;
    }
    
    // agentskills.io specific getters and setters
    
    public boolean isAgentskillsEnabled() {
        return agentskillsEnabled;
    }
    
    public void setAgentskillsEnabled(boolean agentskillsEnabled) {
        this.agentskillsEnabled = agentskillsEnabled;
    }
    
    public boolean isStrictValidation() {
        return strictValidation;
    }
    
    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }
    
    public long getMaxSkillMdSizeKb() {
        return maxSkillMdSizeKb;
    }
    
    public void setMaxSkillMdSizeKb(long maxSkillMdSizeKb) {
        this.maxSkillMdSizeKb = maxSkillMdSizeKb;
    }
    
    public boolean isProgressiveDisclosure() {
        return progressiveDisclosure;
    }
    
    public void setProgressiveDisclosure(boolean progressiveDisclosure) {
        this.progressiveDisclosure = progressiveDisclosure;
    }
    
    public boolean isEnableMetadataCache() {
        return enableMetadataCache;
    }
    
    public void setEnableMetadataCache(boolean enableMetadataCache) {
        this.enableMetadataCache = enableMetadataCache;
    }
}