package com.xue.config;

import java.util.Map;
import java.util.WeakHashMap;

public class TokenCache {
    private static final long EXPIRATION_TIME = 60*60*1000; // 过期时间为60秒
    private static final Map<String, CacheEntry> cache = new WeakHashMap<>();

    public static String getString(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !isExpired(entry)) {
            return entry.getValue();
        } else {
            String value = retrieveValueFromSource(key); // 从数据源获取值
            cache.put(key, new CacheEntry(value));
            return value;
        }
    }

    private static boolean isExpired(CacheEntry entry) {
        long currentTime = System.currentTimeMillis();
        return currentTime - entry.getTimestamp() > EXPIRATION_TIME;
    }

    private static String retrieveValueFromSource(String key) {
        // 从数据源获取值的逻辑
        // 这里只是一个示例，你可以根据实际情况来实现
        return "Value for " + key;
    }

    private static class CacheEntry {
        private final String value;
        private final long timestamp;

        public CacheEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
