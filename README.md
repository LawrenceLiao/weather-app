# Weather Restful Application
## Tech Stack:
* Java 17
* Spring Boot 3.1.2
* Spring Data JPA 3.1.2
* H2 In-Memory Relational Database 2.1.214
* Flyway 9.16.3
* Gradle 8.1.1
* JUnit 5.9.3
* Mockito 5.3.1
* Wiremock 2.35.0
* MockMvc 6.0.11
* Lombok 1.18.28
* Mapstruct 1.5.2

## Topics
1. [Build the application](#build-the-application)
2. [Run the application locally](#run-the-application-locally)
3. [Run all tests](#run-all-tests)
4. [Interact with provided APIs](#interact-with-provided-apis)
5. [Assumptions and Tradeoff](#assumptions-and-tradeoff)
6. [Design and implementation](#design-and-implementation)

## Build the application
* Go the root directory via Terminal or any command line tool
* Execute below command to build the artifact:
  `./gradlew clean build`

## Run the application locally
* Under the root directory of the project
* Run the command to start the application, `apiKey` refers to your own OpenWeatherMap key:
  `./gradlew bootRun --args="--open-weather-map.api.key={apiKey}"`

## Run all tests
* Under the root directory of the project
* Execute below command to run all tests of this project:
  `./gradlew clean test`

## Interact with provided APIs
### `GET /weather`
Call the below endpoint via browser or tools like *Postman*
`http://localhost:8080/api/v1/weather?city={city}&country={country}&token={token}`

Request parameters:
* `city` refers to the full city name(case-ignored), e.g. **melbourne**
* `country`refers to abbreviation for country name(case-ignored), e.g. **au** for Australia
* `token` refers to user's token for this application(case-sensitive)

Example:
`http://localhost:8080/api/v1/weather?city=melbourne&country=au&token=fake-user-token`

## Assumptions and Tradeoff
* Assume weather data expires in 60 minutes
* Assume user token is unique for each user and not updatable
* Assume that requests with invalid input(city, country) still count for access
* Assume default time zone for server is *UTC*
* Assume we ignore rate limit on *OpenWeatherMap* side

## Design and implementation
* Apply input validation via *Spring Validation*
* Use Spring Data JPA as ORM framework to manage entities and repository layer
* Introduce Flyway to do DB version control, including creating tables and inserting initial data
* Make use of AOP based on **Proxy Pattern** and **Sliding Window Algorithm** to implement **RateLimiter** for specified endpoints
* Introduce `ControllerAdvice` to implement errors centralised handling
* Use **OffsetDateTime** with **UTC** to avoid various time zones converting issues
* Using Gradle as project management tool is easier for managing dependencies
* Use Lombok generating getter, constructor etc. to make code neater
* Use Mapstruct to simply objects mapping code
* Apply both unit tests and integration tests via Junit, Mockito, MockMvc and WireMock with high testing coverage for guaranteeing robustness of the application
