package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.vo.SkillMetadataVo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for handling progressive disclosure of agent skills according to agentskills.io specification.
 * Implements the three-tier disclosure approach: discovery, activation, and execution.
 */
@Service
public class ProgressiveDisclosureService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressiveDisclosureService.class);

    @Autowired
    private AgentSkillManager skillManager;

    @Autowired
    private AgentskillsManager agentskillsManager;

    private final Map<String, Long> skillMetadataCacheTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Object> skillMetadataCache = new ConcurrentHashMap<>();

    /**
     * Get skill discovery information for initial system prompt.
     * This is the first tier of progressive disclosure - lightweight metadata.
     *
     * @return List of skill discovery information strings
     */
    public List<String> getSkillDiscoveryInfo() {
        return skillManager.getAllSkills().stream()
                .map(skill -> String.format("%s: %s", skill.getName(), skill.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * Get skill discovery information from a skills directory (agentskills.io format).
     *
     * @param skillsDir Path to the skills directory
     * @return List of skill discovery information strings
     */
    public List<String> getSkillDiscoveryInfoFromDirectory(Path skillsDir) {
        return agentskillsManager.listSkillsInDirectory(skillsDir).stream()
                .map(skill -> skill.getDiscoveryInfo())
                .collect(Collectors.toList());
    }

    /**
     * Get detailed skill information for activation phase.
     * This is the second tier of progressive disclosure - detailed instructions and parameters.
     *
     * @param skillName The name of the skill to activate
     * @return Detailed skill information
     */
    public Map<String, Object> getSkillActivationInfo(String skillName) {
        // Check cache first if caching is enabled
        if (isCacheEnabled()) {
            String cacheKey = "activation_" + skillName;
            Long cachedTime = skillMetadataCacheTimestamps.get(cacheKey);
            
            if (cachedTime != null && !isCacheExpired(cachedTime)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) skillMetadataCache.get(cacheKey);
                if (cachedData != null) {
                    logger.debug("Returning cached activation info for skill: {}", skillName);
                    return cachedData;
                }
            }
        }

        AgentSkill skill = skillManager.getSkill(skillName);
        if (skill == null) {
            logger.warn("Skill not found for activation: {}", skillName);
            return null;
        }

        Map<String, Object> info = new ConcurrentHashMap<>();
        info.put("name", skill.getName());
        info.put("description", skill.getDescription());
        info.put("version", skill.getVersion());
        info.put("required_parameters", skill.getRequiredParameters());
        info.put("optional_parameters", skill.getOptionalParameters());
        info.put("instructions", skill.getInstructions());
        info.put("can_handle_pattern", getCanHandlePattern(skill));

        // Cache the result if caching is enabled
        if (isCacheEnabled()) {
            String cacheKey = "activation_" + skillName;
            skillMetadataCache.put(cacheKey, info);
            skillMetadataCacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }

        return info;
    }

    /**
     * Get activation information for a skill from agentskills.io format.
     *
     * @param skillPath Path to the skill directory
     * @return Detailed skill information
     */
    public Map<String, Object> getSkillActivationInfoFromPath(Path skillPath) {
        SkillMetadataVo metadata = agentskillsManager.getSkillMetadata(skillPath);
        if (metadata == null) {
            return null;
        }

        Map<String, Object> info = new ConcurrentHashMap<>();
        info.put("name", metadata.getName());
        info.put("description", metadata.getFullMetadata().get("description"));
        info.put("version", metadata.getFullMetadata().get("version"));
        info.put("instructions", metadata.getInstructions());
        info.put("argument_hint", metadata.getFullMetadata().get("argument_hint"));
        info.put("disable_model_invocation", metadata.getFullMetadata().get("disable_model_invocation"));
        info.put("user_invocable", metadata.getFullMetadata().get("user_invocable"));
        info.put("allowed_tools", metadata.getFullMetadata().get("allowed_tools"));

        return info;
    }

    /**
     * Get the pattern that determines when a skill can handle a request.
     * This is a simplified representation of the skill's canHandle method.
     */
    private String getCanHandlePattern(AgentSkill skill) {
        // This would typically be derived from the skill's implementation
        // For now, we'll return a placeholder - in a real implementation,
        // this could be derived from annotations or configuration
        return "Dynamic pattern based on skill implementation";
    }

    /**
     * Check if metadata caching is enabled.
     */
    private boolean isCacheEnabled() {
        // In a real implementation, this would check the properties
        // For now, we'll assume it's enabled
        return true;
    }

    /**
     * Check if cache entry is expired.
     */
    private boolean isCacheExpired(Long timestamp) {
        // Cache expiration time: 5 minutes (300,000 ms)
        long cacheDuration = 5 * 60 * 1000;
        return (System.currentTimeMillis() - timestamp) > cacheDuration;
    }

    /**
     * Clear the metadata cache.
     */
    public void clearCache() {
        skillMetadataCache.clear();
        skillMetadataCacheTimestamps.clear();
        logger.debug("Progressive disclosure cache cleared");
    }

    /**
     * Invalidate cache for a specific skill.
     *
     * @param skillName The name of the skill to invalidate
     */
    public void invalidateSkillCache(String skillName) {
        String activationKey = "activation_" + skillName;
        String executionKey = "execution_" + skillName;
        
        skillMetadataCache.remove(activationKey);
        skillMetadataCache.remove(executionKey);
        skillMetadataCacheTimestamps.remove(activationKey);
        skillMetadataCacheTimestamps.remove(executionKey);
        
        logger.debug("Cache invalidated for skill: {}", skillName);
    }

    /**
     * Get execution context for a skill (third tier of progressive disclosure).
     * This includes all necessary information for skill execution.
     *
     * @param skillName The name of the skill
     * @return Execution context
     */
    public Map<String, Object> getSkillExecutionContext(String skillName) {
        Map<String, Object> activationInfo = getSkillActivationInfo(skillName);
        if (activationInfo == null) {
            return null;
        }

        // Add execution-specific information
        activationInfo.put("execution_context", "ready");
        activationInfo.put("available_tools", activationInfo.get("allowed_tools")); // Simplified mapping
        activationInfo.put("execution_constraints", buildExecutionConstraints(activationInfo));

        return activationInfo;
    }

    /**
     * Build execution constraints based on skill metadata.
     */
    private Map<String, Object> buildExecutionConstraints(Map<String, Object> skillInfo) {
        Map<String, Object> constraints = new ConcurrentHashMap<>();
        
        // Add constraints based on metadata
        constraints.put("max_execution_time", 30); // seconds
        constraints.put("input_validation_required", true);
        constraints.put("output_formatting_required", true);
        
        // Add specific constraints based on skill properties
        Boolean disableModelInvocation = (Boolean) skillInfo.get("disable_model_invocation");
        constraints.put("auto_invocation_allowed", disableModelInvocation == null || !disableModelInvocation);
        
        Boolean userInvocable = (Boolean) skillInfo.get("user_invocable");
        constraints.put("manual_invocation_allowed", userInvocable == null || userInvocable);
        
        return constraints;
    }

    /**
     * Prepare a skill for execution by retrieving all necessary information.
     *
     * @param skillName The name of the skill to prepare
     * @return Prepared skill context
     */
    public Map<String, Object> prepareSkillForExecution(String skillName) {
        Map<String, Object> discoveryInfo = new ConcurrentHashMap<>();
        
        // Get all tiers of information progressively
        List<String> discoveryTier = getSkillDiscoveryInfo();
        Map<String, Object> activationTier = getSkillActivationInfo(skillName);
        Map<String, Object> executionTier = getSkillExecutionContext(skillName);
        
        discoveryInfo.put("tier_1_discovery", discoveryTier);
        discoveryInfo.put("tier_2_activation", activationTier);
        discoveryInfo.put("tier_3_execution", executionTier);
        
        return discoveryInfo;
    }
}