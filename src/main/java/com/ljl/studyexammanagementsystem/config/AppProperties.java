package com.ljl.studyexammanagementsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cache cache = new Cache();

    @Data
    public static class Cache {
        private boolean enabled;
        private boolean localCache;
        private String keyPrefix = "exam";
        private long defaultTtlSeconds = 600;
        private long nullTtlSeconds = 60;
        private long lockWaitMillis = 3000;
    }
}
