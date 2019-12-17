# Usage

## Building locally

Stream Registry is built using [OpenJDK 11](https://openjdk.java.net/install/) and Maven. 

Stream Registry is currently packaged as a shaded JAR file.
We leave specific deployment considerations up to each team since this varies from enterprise to enterprise.

To build Stream Registry as a JAR file, please run

```console
./mvnw clean package
```

## Start Stream Registry

> <em>**Required Local Environment**<br/>
> The local 'dev' version of Stream Registry requires a locally running version of Apache Kafka
> and Confluent's Schema Registry on ports 9092 and 8081, respectively.</em>

To quickly get a local dev environment set up, we recommend to use Docker Compose. 

Alternatively, one can start Confluent Platform locally after downloading the [Confluent CLI][confluent-cli-doc] and running the following command.
**Note**: The `confluent` command is currently only available for macOS and Linux. If using Windows, you'll need to use Docker, or run ZooKeeper, Kafka, and the Schema Registry all individually.

  [confluent-cli-doc]: https://docs.confluent.io/current/cli/index.html

```console
confluent start zookeeper
confluent start kafka
confluent start schema-registry
```

Stream Registry can then be started.

Once Stream Registry has started, check that the application's GraphiQL server is running at http://localhost:8080/graphiql

## Kafka Version Compatibility

Stream Registry development and initial deployment started with Kafka 0.11.0 / Confluent Platform 3.3.0, and has also been deployed against Kafka 1.1.1 / Confluent Platform 4.1.2.  
As per the [Kafka Compatibility Matrix][kafka-compatibility-doc], we expect Stream Registry to be compatbile with Kafka 0.10.0 and newer, and the internal Java Kafka clients used by Stream Registry can be found in the [`pom.xml`](pom.xml).

  [kafka-compatibility-doc]: https://cwiki.apache.org/confluence/display/KAFKA/Compatibility+Matrix

## Run Unit Tests
```console
./mvnw clean test
```

