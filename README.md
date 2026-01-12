# SmartHomeApp
Smart home application coursework

SmartHomeApp is a Java-based smart home application developed as coursework.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Project Structure

- `src/main/java`: Application source code
- `src/test/java`: Unit tests

## How to Build

From the project root directory, run:

```sh
mvn clean install
```

## How to Run Tests

To execute all unit tests:

```sh
mvn test
```

## How to Run the Application

Assuming your main class is `com.smarthome.Main`, run:

```sh
mvn exec:java -Dexec.mainClass="com.smarthome.Main"
```

Or, if you prefer to run the compiled JAR (after `mvn package`):

```sh
java -cp target/SmartHomeApp-1.0-SNAPSHOT.jar com.smarthome.Main
```

## Notes

- Update the main class name in the above commands if it differs.
- Ensure all dependencies are specified in `pom.xml`.
