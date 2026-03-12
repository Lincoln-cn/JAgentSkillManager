package org.unreal.agent.skill.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple in-memory cache for storing skill metadata and other frequently accessed data.
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public class SimpleCache<K, V> {
    
    private final Map<K, CachedValue<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMillis;
    private final int maxSize;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public SimpleCache(long ttlMillis, int maxSize) {
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
    }
    
    /**
     * Retrieves a value from the cache.
     * 
     * @param key the key
     * @return the value if found and not expired, null otherwise
     */
    public V get(K key) {
        CachedValue<V> cached = cache.get(key);
        if (cached == null) {
            return null;
        }
        
        if (isExpired(cached)) {
            cache.remove(key); // Remove expired entry
            return null;
        }
        
        return cached.getValue();
    }
    
    /**
     * Puts a value in the cache.
     * 
     * @param key the key
     * @param value the value
     */
    public void put(K key, V value) {
        // Check if we need to evict entries due to size limit
        if (cache.size() >= maxSize) {
            // Simple eviction: remove oldest entries
            cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
            
            // If still over size, remove random entries (in a real implementation, 
            // you'd want a more sophisticated eviction strategy like LRU)
            if (cache.size() >= maxSize) {
                cache.entrySet().iterator().remove();
            }
        }
        
        cache.put(key, new CachedValue<>(value, System.currentTimeMillis()));
    }
    
    /**
     * Removes a value from the cache.
     * 
     * @param key the key
     */
    public void remove(K key) {
        cache.remove(key);
    }
    
    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Checks if a cached value is expired.
     * 
     * @param cachedValue the cached value
     * @return true if expired, false otherwise
     */
    private boolean isExpired(CachedValue<V> cachedValue) {
        return System.currentTimeMillis() - cachedValue.getTimestamp() > ttlMillis;
    }
    
    /**
     * Gets the current size of the cache.
     * 
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * Inner class to hold cached values with timestamps.
     */
    private static class CachedValue<V> {
        private final V value;
        private final long timestamp;
        
        public CachedValue(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
        
        public V getValue() {
            return value;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}