package org.unreal.agent.skill.folder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unreal.agent.skill.AgentSkill;
import org.unreal.agent.skill.AgentSkillManager;
import org.unreal.agent.skill.config.AgentSkillProperties;
import org.unreal.agent.skill.vo.SkillMetadataVo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service for handling progressive disclosure of agent skills according to agentskills.io specification.
 * Implements the three-tier disclosure approach: discovery, activation, and execution.
 * 
 * <p>Cache Features:
 * <ul>
 *     <li>Configurable cache expiration time</li>
 *     <li>Maximum cache size limit with LRU eviction</li>
 *     <li>Cache statistics tracking (hits, misses, evictions)</li>
 *     <li>Per-skill cache invalidation</li>
 * </ul>
 */
@Service
public class ProgressiveDisclosureService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressiveDisclosureService.class);

    @Autowired
    private AgentSkillManager skillManager;

    @Autowired
    private AgentskillsManager agentskillsManager;

    @Autowired
    private AgentSkillProperties skillProperties;

    // Cache storage
    private final Map<String, CacheEntry<Object>> skillMetadataCache = new ConcurrentHashMap<>();
    
    // Cache statistics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);
    private final AtomicLong cachePuts = new AtomicLong(0);

    /**
     * Cache entry wrapper with timestamp and metadata.
     */
    private static class CacheEntry<T> {
        private final T value;
        private final long timestamp;
        private final long accessTime;

        public CacheEntry(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
            this.accessTime = this.timestamp;
        }

        public T getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getAccessTime() {
            return accessTime;
        }
    }

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
        String cacheKey = "activation_" + skillName;

        // Check cache first if caching is enabled
        if (isCacheEnabled()) {
            Object cachedData = getCachedValue(cacheKey);
            if (cachedData != null) {
                logger.debug("Returning cached activation info for skill: {}", skillName);
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) cachedData;
                return result;
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
            putCacheValue(cacheKey, info);
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
        return skillProperties != null && skillProperties.isEnableMetadataCache();
    }

    /**
     * Get value from cache if it exists and is not expired.
     * Updates access time for LRU tracking.
     *
     * @param key The cache key
     * @return The cached value or null if not found/expired
     */
    @SuppressWarnings("unchecked")
    private <T> T getCachedValue(String key) {
        CacheEntry<T> entry = (CacheEntry<T>) skillMetadataCache.get(key);
        if (entry == null) {
            cacheMisses.incrementAndGet();
            logger.trace("Cache miss for key: {}", key);
            return null;
        }

        // Check if expired
        if (isCacheExpired(entry.getTimestamp())) {
            skillMetadataCache.remove(key);
            cacheEvictions.incrementAndGet();
            logger.trace("Cache expired for key: {}", key);
            cacheMisses.incrementAndGet();
            return null;
        }

        cacheHits.incrementAndGet();
        logger.trace("Cache hit for key: {}", key);
        return entry.getValue();
    }

    /**
     * Put value into cache with LRU eviction if necessary.
     *
     * @param key The cache key
     * @param value The value to cache
     */
    private void putCacheValue(String key, Object value) {
        // Check if we need to evict entries due to size limit
        if (skillProperties != null && skillMetadataCache.size() >= skillProperties.getMaxCacheSize()) {
            if (!skillMetadataCache.containsKey(key)) {
                // Only evict if this is a new key
                evictLRUEntry();
            }
        }

        skillMetadataCache.put(key, new CacheEntry<>(value));
        cachePuts.incrementAndGet();
        logger.trace("Cached value for key: {}, cache size: {}", key, skillMetadataCache.size());
    }

    /**
     * Evict the least recently used entry (oldest timestamp).
     */
    private void evictLRUEntry() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (Map.Entry<String, CacheEntry<Object>> entry : skillMetadataCache.entrySet()) {
            if (entry.getValue().getTimestamp() < oldestTime) {
                oldestTime = entry.getValue().getTimestamp();
                oldestKey = entry.getKey();
            }
        }

        if (oldestKey != null) {
            skillMetadataCache.remove(oldestKey);
            cacheEvictions.incrementAndGet();
            logger.debug("Evicted LRU entry: {}", oldestKey);
        }
    }

    /**
     * Check if cache entry is expired.
     *
     * @param timestamp The timestamp to check
     * @return true if expired, false otherwise
     */
    private boolean isCacheExpired(Long timestamp) {
        long cacheDuration = skillProperties != null 
            ? skillProperties.getCacheExpirationMs() 
            : 5 * 60 * 1000;
        return (System.currentTimeMillis() - timestamp) > cacheDuration;
    }

    /**
     * Clear the metadata cache.
     */
    public void clearCache() {
        int size = skillMetadataCache.size();
        skillMetadataCache.clear();
        logger.info("Progressive disclosure cache cleared, removed {} entries", size);
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

    // ==================== Cache Statistics Methods ====================

    /**
     * Get cache hit count.
     *
     * @return Number of cache hits
     */
    public long getCacheHits() {
        return cacheHits.get();
    }

    /**
     * Get cache miss count.
     *
     * @return Number of cache misses
     */
    public long getCacheMisses() {
        return cacheMisses.get();
    }

    /**
     * Get cache eviction count.
     *
     * @return Number of cache evictions
     */
    public long getCacheEvictions() {
        return cacheEvictions.get();
    }

    /**
     * Get cache put count.
     *
     * @return Number of cache puts
     */
    public long getCachePuts() {
        return cachePuts.get();
    }

    /**
     * Get current cache size.
     *
     * @return Number of entries in cache
     */
    public int getCacheSize() {
        return skillMetadataCache.size();
    }

    /**
     * Get cache hit rate as a percentage.
     *
     * @return Hit rate percentage (0-100), or -1 if stats are disabled
     */
    public double getCacheHitRate() {
        if (skillProperties == null || !skillProperties.isEnableCacheStats()) {
            return -1.0;
        }

        long totalRequests = cacheHits.get() + cacheMisses.get();
        if (totalRequests == 0) {
            return 0.0;
        }

        return (double) cacheHits.get() / totalRequests * 100.0;
    }

    /**
     * Get cache miss rate as a percentage.
     *
     * @return Miss rate percentage (0-100), or -1 if stats are disabled
     */
    public double getCacheMissRate() {
        if (skillProperties == null || !skillProperties.isEnableCacheStats()) {
            return -1.0;
        }

        long totalRequests = cacheHits.get() + cacheMisses.get();
        if (totalRequests == 0) {
            return 0.0;
        }

        return (double) cacheMisses.get() / totalRequests * 100.0;
    }

    /**
     * Reset cache statistics counters.
     */
    public void resetCacheStats() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cacheEvictions.set(0);
        cachePuts.set(0);
        logger.info("Cache statistics reset");
    }

    /**
     * Get cache statistics summary.
     *
     * @return Map containing cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("size", getCacheSize());
        stats.put("hits", getCacheHits());
        stats.put("misses", getCacheMisses());
        stats.put("evictions", getCacheEvictions());
        stats.put("puts", getCachePuts());
        stats.put("hit_rate", getCacheHitRate());
        stats.put("miss_rate", getCacheMissRate());
        stats.put("enabled", isCacheEnabled());
        stats.put("max_size", skillProperties != null ? skillProperties.getMaxCacheSize() : 100);
        stats.put("expiration_ms", skillProperties != null ? skillProperties.getCacheExpirationMs() : 300000);
        return stats;
    }

    /**
     * Log cache statistics to the logger.
     */
    public void logCacheStats() {
        if (skillProperties == null || !skillProperties.isEnableCacheStats()) {
            logger.debug("Cache statistics are disabled");
            return;
        }

        Map<String, Object> stats = getCacheStats();
        logger.info("=== Cache Statistics ===");
        logger.info("Size: {} / {}", stats.get("size"), stats.get("max_size"));
        logger.info("Hits: {}, Misses: {}, Evictions: {}, Puts: {}", 
                stats.get("hits"), stats.get("misses"), stats.get("evictions"), stats.get("puts"));
        logger.info("Hit Rate: {:.2f}%, Miss Rate: {:.2f}%", 
                stats.get("hit_rate"), stats.get("miss_rate"));
        logger.info("Expiration: {} ms", stats.get("expiration_ms"));
        logger.info("========================");
    }
}
