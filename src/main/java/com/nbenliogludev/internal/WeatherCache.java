package com.nbenliogludev.internal;

import com.nbenliogludev.model.WeatherResponse;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author nbenliogludev
 */
public class WeatherCache {

    private final int maxSize;
    private final long ttlMillis;

    private final LinkedHashMap<String, CachedEntry> map;

    public WeatherCache(int maxSize, long ttlMillis) {
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
        this.map = new LinkedHashMap<String, CachedEntry>(16, 0.75f, true);
    }

    public synchronized WeatherResponse getIfFresh(String key) {
        CachedEntry entry = map.get(key);
        if (entry == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        if (now - entry.fetchedAtMillis > ttlMillis) {
            // просрочено — удаляем и возвращаем null
            map.remove(key);
            return null;
        }

        return entry.response;
    }

    public synchronized void put(String key, WeatherResponse response) {
        map.put(key, new CachedEntry(response, System.currentTimeMillis()));

        if (map.size() > maxSize) {
            Iterator<String> it = map.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }

    public synchronized Map<String, WeatherResponse> snapshot() {
        Map<String, WeatherResponse> copy =
                new LinkedHashMap<String, WeatherResponse>();
        for (Map.Entry<String, CachedEntry> e : map.entrySet()) {
            copy.put(e.getKey(), e.getValue().response);
        }
        return copy;
    }

    private static class CachedEntry {
        private final WeatherResponse response;
        private final long fetchedAtMillis;

        private CachedEntry(WeatherResponse response, long fetchedAtMillis) {
            this.response = response;
            this.fetchedAtMillis = fetchedAtMillis;
        }
    }
}
