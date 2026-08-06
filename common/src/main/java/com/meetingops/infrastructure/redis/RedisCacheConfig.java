package com.meetingops.infrastructure.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
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
 * Distributed Redis Cache configuration for memory optimization.
 * Configures explicit TTLs to prevent memory growth and cache bloat.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = false)
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // Explicit Cache TTL configurations for hot vs. warm data
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Meeting DTO summaries cache (15 minutes TTL)
        cacheConfigurations.put("meetings-summary", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // User session / role cache (1 hour TTL)
        cacheConfigurations.put("user-roles", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Review queue cache (5 minutes TTL)
        cacheConfigurations.put("review-queue", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
