package com.example.usermanagement.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration.
 *
 * @EnableCaching: Enables Spring's caching abstraction.
 * This allows @Cacheable, @CacheEvict, @CachePut annotations to work.
 *
 * CACHING ANNOTATIONS EXPLAINED:
 *
 * @Cacheable("users")
 * - Check cache FIRST
 * - If found (HIT): return cached value, skip method
 * - If not found (MISS): execute method, store result in cache
 *
 * @CachePut("users")
 * - ALWAYS execute the method
 * - Update cache with new result
 * - Use for UPDATE operations
 *
 * @CacheEvict("users")
 * - Remove entry from cache
 * - Use for DELETE operations
 * - allEntries=true: Clear entire cache
 *
 * @Caching(...)
 * - Combine multiple cache operations
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure Redis Cache Manager with custom settings per cache.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // Default TTL: 10 minutes
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();  // Don't cache null values

        // Custom TTL for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // "users" cache: 30 minutes TTL
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // "usersList" cache: 5 minutes TTL (lists change more frequently)
        cacheConfigurations.put("usersList", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // "userByEmail" cache: 30 minutes TTL
        cacheConfigurations.put("userByEmail", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
