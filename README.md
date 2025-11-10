# Weather SDK (Java)

A small Java SDK for accessing the [OpenWeather](https://openweathermap.org/api) **Current Weather** API.

The SDK is designed to be:

- easy to integrate in other Java applications,
- safe and predictable (typed models, clear error handling),
- efficient (caching + two operating modes: ON_DEMAND and POLLING).

---

## Features

- Java 8 compatible SDK.
- Simple client API:
    - `WeatherClient#getCurrentWeather(String city)`
    - `WeatherClient#getCurrentWeatherJson(String city)`
- Two modes of operation:
    - **ON_DEMAND** — updates data only on user request.
    - **POLLING** — periodically refreshes cached cities in the background.
- In-memory cache:
    - up to **10 cities**,
    - entries are considered **fresh for 10 minutes**,
    - LRU eviction strategy.
- Normalized JSON response returned by the SDK:
    - independent from OpenWeather's raw response.
- Detailed exception hierarchy:
    - network errors,
    - API errors,
    - city not found,
    - parsing errors.
- Support for multiple API keys with **one client per key** and explicit `destroy`.

---

## Requirements

- **Java:** 8+
- **Build tool:** Maven 3.6+
- Internet access to call the OpenWeather API.

---

## Installation

### Build from source

Clone the repository and run:

```bash
mvn clean install
```

---

## Tests

### The project includes both unit and integration tests

Run tests:

```bash
mvn test
```

## Installation (via JitPack)

### 1. Add JitPack repository

In your **application** `pom.xml` (not in the SDK itself), add:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
  ```
Add dependency on the SDK

```xml
  <dependencies>
      <dependency>
          <groupId>com.github.nbenliogludev</groupId>
          <artifactId>weather-sdk-java</artifactId>
          <version>main-SNAPSHOT</version>
      </dependency>
  </dependencies>
  ```
After this, run:

```bash
mvn clean compile
```

## Setting the OpenWeather API key

You need an API key from OpenWeather. The SDK itself accepts a String apiKey.

# Basic Usage Examples

## Example 1: ON_DEMAND mode

```bash
  WeatherClient client = WeatherClients.create(apiKey, Mode.ON_DEMAND);

  WeatherResponse response = client.getCurrentWeather(city);

  System.out.println("City: " + response.getName());
  System.out.println("Weather: " + response.getWeather().getMain()
          + " (" + response.getWeather().getDescription() + ")");
  System.out.println("Temperature: " + response.getTemperature().getTemp());
  System.out.println("Feels like: " + response.getTemperature().getFeelsLike());
  System.out.println("Wind speed: " + response.getWind().getSpeed());

  String json = client.getCurrentWeatherJson(city);
  System.out.println("\nJSON from SDK:");
  System.out.println(json);

  WeatherClients.destroy(apiKey);
```

**ON_DEMAND behavior**

On each call:

- If there is a **fresh** cache entry for the city (younger than 10 minutes),  
  the SDK returns it immediately from memory.
- Otherwise:
  - calls the OpenWeather API,
  - updates the cache with the fresh data,
  - returns the fresh response.

In this mode **no background threads** are created.

## Example 2: POLLING mode

```bash
WeatherClient client = WeatherClients.create(apiKey, Mode.POLLING);

System.out.println("=== First call (may hit API) ===");
WeatherResponse first = client.getCurrentWeather(city);
System.out.println("Temp: " + first.getTemperature().getTemp());

System.out.println("\nWaiting 15 seconds to let polling refresh cache...");
Thread.sleep(15_000L);

System.out.println("\n=== Second call (likely from cache) ===");
WeatherResponse second = client.getCurrentWeather(city);
System.out.println("Temp: " + second.getTemperature().getTemp());

WeatherClients.destroy(apiKey);
```

**POLLING behavior**

- The **first** call for a given city:
  - hits the OpenWeather API,
  - stores the result in the in-memory cache.

- A background scheduler periodically refreshes **all cached cities** from OpenWeather.

- As long as polling is successful:
  - cache entries remain fresh,
  - most `getCurrentWeather` calls return immediately from memory (near zero latency).



