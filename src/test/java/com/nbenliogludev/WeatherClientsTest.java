package com.nbenliogludev;

import com.nbenliogludev.exception.WeatherSdkException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author nbenliogludev
 */
class WeatherClientsTest {

    @AfterEach
    void tearDown() {
        WeatherClients.destroy("test-key-1");
        WeatherClients.destroy("test-key-2");
        WeatherClients.destroy("duplicate-key");
    }

    @Test
    void createThrowsWhenApiKeyEmpty() {
        assertThrows(WeatherSdkException.class, () ->
                WeatherClients.create("   ", Mode.ON_DEMAND));

        assertThrows(WeatherSdkException.class, () ->
                WeatherClients.create(null, Mode.ON_DEMAND));
    }

    @Test
    void createThrowsWhenModeNull() {
        WeatherSdkException ex = assertThrows(WeatherSdkException.class, () ->
                WeatherClients.create("test-key-1", null));

        assertTrue(ex.getMessage().contains("Mode must not be null"));
    }

    @Test
    void createReturnsClientAndGetReturnsSameInstance() throws WeatherSdkException {
        WeatherClient client = WeatherClients.create("test-key-1", Mode.ON_DEMAND);

        assertNotNull(client);

        WeatherClient fromRegistry = WeatherClients.get("test-key-1");

        assertSame(client, fromRegistry);
    }

    @Test
    void cannotCreateTwoClientsWithSameKey() throws WeatherSdkException {
        WeatherClient client = WeatherClients.create("duplicate-key", Mode.ON_DEMAND);
        assertNotNull(client);

        WeatherSdkException ex = assertThrows(WeatherSdkException.class, () ->
                WeatherClients.create("duplicate-key", Mode.ON_DEMAND));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void destroyRemovesClientFromRegistry() throws WeatherSdkException {
        WeatherClient client = WeatherClients.create("test-key-2", Mode.ON_DEMAND);
        assertNotNull(client);

        WeatherClients.destroy("test-key-2");

        WeatherClient afterDestroy = WeatherClients.get("test-key-2");
        assertNull(afterDestroy);
    }
}