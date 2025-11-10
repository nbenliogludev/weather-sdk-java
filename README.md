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

### The project includes both unit and integration tests.

#### Running tests

```bash
mvn test
```


