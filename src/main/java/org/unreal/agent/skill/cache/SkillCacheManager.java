package org.unreal.agent.skill.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.unreal.agent.skill.config.AgentSkillProperties;

/**
 * Cache manager for the Agent Skill framework.
 */
@Component
public class SkillCacheManager {
    
    private final SimpleCache<String, Object> skillMetadataCache;
    private final SimpleCache<String, Object> skillExecutionCache;
    
    @Autowired
    private AgentSkillProperties properties;
    
    public SkillCacheManager(AgentSkillProperties properties) {
        // Initialize caches based on configuration
        long ttl = properties.getCacheExpirationMs();
        int maxSize = properties.getMaxCacheSize();
        
        this.skillMetadataCache = new SimpleCache<>(ttl, maxSize);
        this.skillExecutionCache = new SimpleCache<>(ttl / 2, maxSize / 2); // Shorter TTL for execution results
    }
    
    /**
     * Gets the skill metadata cache.
     * 
     * @return the skill metadata cache
     */
    public SimpleCache<String, Object> getSkillMetadataCache() {
        return skillMetadataCache;
    }
    
    /**
     * Gets the skill execution cache.
     * 
     * @return the skill execution cache
     */
    public SimpleCache<String, Object> getSkillExecutionCache() {
        return skillExecutionCache;
    }
    
    /**
     * Gets a value from the skill metadata cache.
     * 
     * @param key the cache key
     * @return the cached value or null
     */
    public Object getSkillMetadata(String key) {
        return skillMetadataCache.get(key);
    }
    
    /**
     * Puts a value in the skill metadata cache.
     * 
     * @param key the cache key
     * @param value the value to cache
     */
    public void putSkillMetadata(String key, Object value) {
        skillMetadataCache.put(key, value);
    }
    
    /**
     * Gets a value from the skill execution cache.
     * 
     * @param key the cache key
     * @return the cached value or null
     */
    public Object getSkillExecutionResult(String key) {
        return skillExecutionCache.get(key);
    }
    
    /**
     * Puts a value in the skill execution cache.
     * 
     * @param key the cache key
     * @param value the value to cache
     */
    public void putSkillExecutionResult(String key, Object value) {
        skillExecutionCache.put(key, value);
    }
    
    /**
     * Clears all caches.
     */
    public void clearAllCaches() {
        skillMetadataCache.clear();
        skillExecutionCache.clear();
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return cache statistics
     */
    public CacheStatistics getCacheStatistics() {
        return new CacheStatistics(
            skillMetadataCache.size(),
            skillExecutionCache.size()
        );
    }
    
    /**
     * Cache statistics container.
     */
    public static class CacheStatistics {
        private final int metadataCacheSize;
        private final int executionCacheSize;
        
        public CacheStatistics(int metadataCacheSize, int executionCacheSize) {
            this.metadataCacheSize = metadataCacheSize;
            this.executionCacheSize = executionCacheSize;
        }
        
        public int getMetadataCacheSize() {
            return metadataCacheSize;
        }
        
        public int getExecutionCacheSize() {
            return executionCacheSize;
        }
        
        public int getTotalCacheSize() {
            return metadataCacheSize + executionCacheSize;
        }
    }
}