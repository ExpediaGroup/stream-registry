# Usage

## Building locally

Stream Registry is built using [OpenJDK 11](https://openjdk.java.net/install/) and Maven. For convenience, we have wrapped each Maven command in a [`Makefile`](Makefile).
If you do not have `make` installed, please consult this file for build commands.

Stream Registry is currently packaged as a shaded JAR file.
We leave specific deployment considerations up to each team since this varies from enterprise to enterprise.
We, do, however provide a vanilla Docker example for teams to use/leverage for demo, learning, or development purposes.

To build Stream Registry as a JAR file, please run

```console
make build
```

To build Stream Registry as a Docker image, please run the following, which will use the [Jib](https://github.com/GoogleContainerTools/jib) Maven Plugin to build and install the image

```console
make build-docker
```

## Start Stream Registry

> <em>**Required Local Environment**<br/>
> The local 'dev' version of Stream Registry requires a locally running version of Apache Kafka
> and Confluent's Schema Registry on ports 9092 and 8081, respectively.</em>

To quickly get a local dev environment set up, we recommend to use the provided [Docker Compose](docker-compose.yml).
Be sure to first build the Docker image using the command above.

```console
docker-compose up
```

Alternatively, one can start Confluent Platform locally after downloading the [Confluent CLI][confluent-cli-doc] and running the following command.
**Note**: The `confluent` command is currently only available for macOS and Linux. If using Windows, you'll need to use Docker, or run ZooKeeper, Kafka, and the Schema Registry all individually.

  [confluent-cli-doc]: https://docs.confluent.io/current/cli/index.html

```console
confluent start zookeeper
confluent start kafka
confluent start schema-registry
```

Stream Registry can then be started 

```console
make run
```

Once Stream Registry has started, check that the application's Swagger API is running at http://localhost:8080/swagger

## Create a Stream Locally

Once stream registry is up insert your local cluster info

```bash
curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
     "clusterKey": {
       "vpc": "localRegion",
       "env": "local",
       "hint": "primary",
       "type": null
     },
     "clusterValue": {
       "clusterName": "localCluster",
       "bootstrapServers": "localhost:9092",
       "zookeeperQuorum": "zookeeper:2181",
       "schemaRegistryURL": "http://localhost:8081"
     }
   }' 'http://localhost:8080/v0/clusters'
```

Now, add a stream using `vpcList` as `local` and `tags`->`hint` as `primary` 

Here is a sample stream 
```bash
curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "name": "sampleStream",
  "schemaCompatibility": "BACKWARD",
  "latestKeySchema": {
    "id": "string",
    "version": 0,
    "schemaString": "\"string\"",
    "created": "string",
    "updated": "string"
  },
  "latestValueSchema": {
    "id": "string",
    "version": 0,
    "schemaString": "\"string\"",
    "created": "string",
    "updated": "string"
  },
  "owner": "string",
  "created": 0,
  "updated": 0,
  "githubUrl": "http://github.com",
  "isDataNeededAtRest": true,
  "isAutomationNeeded": true,
  "tags": {
    "productId": 0,
    "portfolioId": 0,
    "brand": "string",
    "assetProtectionLevel": "string",
    "componentId": "string",
    "hint": "primary"
  },
  "vpcList": [
    "localRegion"
  ],
  "replicatedVpcList": [
  ],
  "topicConfig": {},
  "partitions": 1,
  "replicationFactor": 1
}' 'http://localhost:8080/v0/streams/sampleStream'
```

## Kafka Version Compatibility

Stream Registry development and initial deployment started with Kafka 0.11.0 / Confluent Platform 3.3.0, and has also been deployed against Kafka 1.1.1 / Confluent Platform 4.1.2.  
As per the [Kafka Compatibility Matrix][kafka-compatibility-doc], we expect Stream Registry to be compatbile with Kafka 0.10.0 and newer, and the internal Java Kafka clients used by Stream Registry can be found in the [`pom.xml`](pom.xml).

  [kafka-compatibility-doc]: https://cwiki.apache.org/confluence/display/KAFKA/Compatibility+Matrix

## Run Unit Tests
```console
make tests
```

