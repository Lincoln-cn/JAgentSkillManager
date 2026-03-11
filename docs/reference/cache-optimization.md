# 渐进式披露缓存优化文档

## 概述

本文档描述了 `ProgressiveDisclosureService` 的缓存优化改进，解决了原有设计的不足，提高了系统运行效率。

## 优化内容

### 1. 修复配置读取问题

**问题**：`isCacheEnabled()` 方法硬编码返回 `true`，未读取实际配置。

**解决方案**：
```java
@Autowired
private AgentSkillProperties skillProperties;

private boolean isCacheEnabled() {
    return skillProperties != null && skillProperties.isEnableMetadataCache();
}
```

### 2. 可配置的缓存过期时间

**问题**：固定 5 分钟过期时间，无法调整。

**解决方案**：
- 新增配置属性 `cacheExpirationMs`
- 默认值：300000ms（5 分钟）
- 可通过配置文件自定义

```yaml
agent:
  skill:
    cache-expiration-ms: 300000
```

### 3. 缓存大小限制与 LRU 驱逐

**问题**：无缓存大小限制，可能导致内存问题。

**解决方案**：
- 新增配置属性 `maxCacheSize`
- 默认值：100 个条目
- 实现 LRU（最近最少使用）驱逐算法

```java
private void putCacheValue(String key, Object value) {
    if (skillMetadataCache.size() >= skillProperties.getMaxCacheSize()) {
        if (!skillMetadataCache.containsKey(key)) {
            evictLRUEntry();
        }
    }
    skillMetadataCache.put(key, new CacheEntry<>(value));
}
```

### 4. 缓存统计与监控

**问题**：无缓存命中率统计，难以监控性能。

**解决方案**：
- 新增配置属性 `enableCacheStats`
- 使用 `AtomicLong` 统计缓存命中、未命中、驱逐次数
- 提供命中率计算方法

**统计指标**：
- `cacheHits` - 缓存命中次数
- `cacheMisses` - 缓存未命中次数
- `cacheEvictions` - 缓存驱逐次数
- `cachePuts` - 缓存写入次数
- `hitRate` - 命中率（百分比）
- `missRate` - 未命中率（百分比）

## 配置属性说明

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enable-metadata-cache` | boolean | true | 是否启用元数据缓存 |
| `cache-expiration-ms` | long | 300000 | 缓存过期时间（毫秒） |
| `max-cache-size` | int | 100 | 最大缓存条目数 |
| `enable-cache-stats` | boolean | false | 是否启用缓存统计 |

## 使用示例

### 1. 基本使用

```java
@Autowired
private ProgressiveDisclosureService disclosureService;

// 获取技能激活信息（自动缓存）
Map<String, Object> info = disclosureService.getSkillActivationInfo("my-skill");
```

### 2. 缓存管理

```java
// 清除所有缓存
disclosureService.clearCache();

// 清除特定技能的缓存
disclosureService.invalidateSkillCache("my-skill");
```

### 3. 缓存监控

```java
// 获取缓存统计
Map<String, Object> stats = disclosureService.getCacheStats();
System.out.println("缓存大小：" + stats.get("size"));
System.out.println("命中率：" + stats.get("hit_rate") + "%");

// 打印缓存统计日志
disclosureService.logCacheStats();

// 重置统计
disclosureService.resetCacheStats();
```

### 4. 配置示例

```yaml
agent:
  skill:
    # 启用缓存
    enable-metadata-cache: true
    
    # 缓存 10 分钟过期
    cache-expiration-ms: 600000
    
    # 最多缓存 200 个条目
    max-cache-size: 200
    
    # 启用统计监控
    enable-cache-stats: true
```

## 内部实现细节

### CacheEntry 结构

```java
private static class CacheEntry<T> {
    private final T value;        // 缓存值
    private final long timestamp; // 创建时间戳
    private final long accessTime; // 访问时间戳（用于 LRU）
}
```

### LRU 驱逐算法

```java
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
    }
}
```

## 性能建议

### 开发环境
```yaml
agent:
  skill:
    enable-metadata-cache: true
    cache-expiration-ms: 60000    # 1 分钟，快速看到变更
    max-cache-size: 50            # 较小的缓存
    enable-cache-stats: true      # 启用统计便于调试
```

### 生产环境
```yaml
agent:
  skill:
    enable-metadata-cache: true
    cache-expiration-ms: 600000   # 10 分钟，减少重复解析
    max-cache-size: 200           # 较大的缓存
    enable-cache-stats: true      # 启用统计便于监控
```

## API 参考

### 缓存操作方法

| 方法 | 说明 |
|------|------|
| `clearCache()` | 清空所有缓存 |
| `invalidateSkillCache(String skillName)` | 使特定技能的缓存失效 |
| `getCacheSize()` | 获取当前缓存大小 |

### 统计操作方法

| 方法 | 说明 |
|------|------|
| `getCacheHits()` | 获取命中次数 |
| `getCacheMisses()` | 获取未命中次数 |
| `getCacheEvictions()` | 获取驱逐次数 |
| `getCacheHitRate()` | 获取命中率（%） |
| `getCacheMissRate()` | 获取未命中率（%） |
| `getCacheStats()` | 获取完整统计信息 |
| `resetCacheStats()` | 重置统计数据 |
| `logCacheStats()` | 打印统计日志 |

## 监控指标说明

### 命中率（Hit Rate）
- **计算公式**：`hits / (hits + misses) * 100%`
- **理想值**：> 80%
- **优化建议**：如果命中率过低，考虑增加缓存大小或延长过期时间

### 驱逐率（Eviction Rate）
- **计算公式**：`evictions / puts * 100%`
- **理想值**：< 20%
- **优化建议**：如果驱逐率过高，考虑增加缓存大小

### 缓存大小
- **监控**：当前缓存条目数 / 最大缓存大小
- **建议**：保持在最大值的 50%-80% 之间

## 变更记录

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-03-11 | 1.0 | 初始版本，添加缓存优化功能 |
