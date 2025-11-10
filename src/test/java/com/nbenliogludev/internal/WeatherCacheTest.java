package com.nbenliogludev.internal;

import com.nbenliogludev.model.WeatherResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author nbenliogludev
 */
class WeatherCacheTest {

    @Test
    void getIfFreshReturnsNullWhenKeyMissing() {
        WeatherCache cache = new WeatherCache(10, 600_000L);

        WeatherResponse result = cache.getIfFresh("london");

        assertNull(result);
    }

    @Test
    void getIfFreshReturnsValueWhenNotExpired() {
        WeatherCache cache = new WeatherCache(10, 600_000L);
        WeatherResponse response = new WeatherResponse();
        response.setName("London");

        cache.put("london", response);

        WeatherResponse cached = cache.getIfFresh("london");

        assertNotNull(cached);
        assertEquals("London", cached.getName());
    }

    @Test
    void getIfFreshRemovesEntryWhenExpired() throws InterruptedException {
        WeatherCache cache = new WeatherCache(10, 100L);
        WeatherResponse response = new WeatherResponse();

        cache.put("city", response);

        Thread.sleep(150L);

        WeatherResponse cached = cache.getIfFresh("city");

        assertNull(cached);

        Map<String, WeatherResponse> snapshot = cache.snapshot();
        assertFalse(snapshot.containsKey("city"));
    }

    @Test
    void putEvictsLeastRecentlyUsedWhenMaxSizeExceeded() {
        WeatherCache cache = new WeatherCache(2, 600_000L);

        WeatherResponse r1 = new WeatherResponse();
        r1.setName("City1");
        WeatherResponse r2 = new WeatherResponse();
        r2.setName("City2");
        WeatherResponse r3 = new WeatherResponse();
        r3.setName("City3");

        cache.put("city1", r1);
        cache.put("city2", r2);

        cache.getIfFresh("city1"); // обновляем порядок: city2 становится LRU

        cache.put("city3", r3);

        Map<String, WeatherResponse> snapshot = cache.snapshot();

        assertEquals(2, snapshot.size());
        assertTrue(snapshot.containsKey("city1"));
        assertTrue(snapshot.containsKey("city3"));
        assertFalse(snapshot.containsKey("city2"));
    }
}