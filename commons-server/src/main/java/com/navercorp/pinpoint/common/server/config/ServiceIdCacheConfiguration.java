package com.navercorp.pinpoint.common.server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Import(CommonCacheManagerConfiguration.class)
@ConditionalOnProperty(name = "pinpoint.web.v4.enable", havingValue = "true")
public class ServiceIdCacheConfiguration {

    public static final String SERVICE_ID_CACHE_NAME = "serviceIdCache";
    public static final String SERVICE_NAME_CACHE_NAME = "serviceNameCache";

    @Bean
    public CacheManager serviceIdCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_ID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }

    @Bean
    public CacheManager serviceNameCache() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(SERVICE_NAME_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .initialCapacity(10)
                .maximumSize(200));
        return caffeineCacheManager;
    }
}
