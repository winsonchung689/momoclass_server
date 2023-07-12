package com.xue.config;

import java.util.Map;
import java.util.WeakHashMap;

public class TokenCache {
    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 过期时间为1分钟
    private static final Map<String, CacheEntry> cache = new WeakHashMap<>();

    private static class CacheEntry {
        private final String value;
        private final long expirationTime;

        public CacheEntry(String value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    public static synchronized String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        } else {
            return null;
        }
    }

    public static synchronized void put(String key, String value) {
        long expirationTime = System.currentTimeMillis() + EXPIRATION_TIME;
        CacheEntry entry = new CacheEntry(value, expirationTime);
        cache.put(key, entry);
    }
}
