package org.tommy.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import java.net.URISyntaxException;

@TestConfiguration
public class AppConfig {
    @Bean
    public CacheManager cacheManager() throws URISyntaxException {
        return new JCacheCacheManager(Caching.getCachingProvider().getCacheManager(
                getClass().getResource("/ehcache.xml").toURI(),
                getClass().getClassLoader()
        ));
    }
}
