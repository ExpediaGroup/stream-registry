# stream-registry [![Build Status][build-icon]][build-link]

[build-icon]: https://travis-ci.org/homeaway/stream-registry.svg?branch=master
[build-link]: https://travis-ci.org/homeaway/stream-registry

<center>
<img src="docs/docs/architecture/SR-logo.svg" alt="StreamRegistryLogo" style="max-width:50%;"/>
</center>

# Announcement: 12th April 2019
We wanted to let you know that there are going to be some exciting developments with the Stream Registry project in the very near future. Stream Registry is being adopted by many brands at Expedia Group as a critical component of its digital nervous system for key streams across Expedia Group. Therefore, HomeAway stream registry is finding a new home.

## What is changing
* We will be investing in the project by expanding the existing team with full-time resources in several locations across Expedia Group. Expect greatly increased project activity: contributors, commits, issues, features, releases
* The repository will relocate to the [ExpediaGroup open source GitHub org](https://github.com/ExpediaGroup) in its entirety, preserving the history and community

## What isn't changing
* The original vision of Stream Registry as a Stream Discovery and Stream Orchestration platform
* The project will remain open source, and will be joined shortly by other supporting Expedia Group stream platform components
* Licenses, conduct and contribution guidelines will remain unchanged
* The value of your contributions - please keep them coming!

We expect the start of this journey to be a little bumpy, but please bear with us as we work towards the first release of the **Expedia Group** Stream Registry!

# About
A Stream Registry is what its name implies: it is a registry of streams. As enterprises increasingly scale in size,
the need to organize and develop around streams of data becomes paramount.  Synchronous calls are attracted to the edge,
and a variety of synchronous and asynchronous calls permeate the enterprise.  The need for a declarative, central
authority for discovery and orchestration of stream management emerges.  This is what a stream registry provides.
In much the same way that DNS provides a name translation service for an ip address, by way of analogy, a
Stream Registry provides a “metadata service” for streams. By centralizing stream metadata, a stream translation service
for producer and/or consumer stream coördinates becomes possible. This centralized, yet democratized, stream metadata
function thus streamlines operational complexity via stream lifecycle management, stream discovery,
stream availability and resiliency.

## Why Stream Registry?

We believe that as the change to business requirements accelerate, time to market pressures increase,
competitive measures grow, migrations to cloud and different platforms are required, and so on, systems will
increasingly need to become more reactive and dynamic in nature.

<p align="center">The issue of <em>state</em> arises.</p>

We see many systems adopting _event-driven-architectures_ to facilitate the changing business needs in these high stakes
environments.  We hypothesize there is an emerging need for a centralized "stream metadata" service in the
industry to help streamline the complexities and operations of deploying stream platforms that serve as a
distributed federated nervous system in the enterprise.

## What is Stream Registry?
Put simply, Stream Registry is a centralized service for stream metadata.

The stream registry can answer the following question:

1. Who owns the stream?  
2. Who are the producers and consumers of the stream?  
3. Management of stream replication across clusters and regions
4. Management of stream storage for permanent access
5. Management of stream triggers for legacy stream sources

## Architecture

<center>
<img src="docs/docs/architecture/StreamRegistryArchitecture.png" alt="StreamRegistryArchitecture"/>
</center>

See the [architecture/northstar documentation](https://homeaway.github.io/stream-registry/) for more details.

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

First create your cluster

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

Now, declare your stream

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

## Contributors
Special thanks to the following for making stream-registry possible at HomeAway and beyond!

<!-- Contributors START
Adam_Westerman westeras https://www.linkedin.com/in/adam-westerman/ code
Arun_Vasudevan arunvasudevan https://www.linkedin.com/in/arun-vasudevan-55117368/ code design
Nathan_Walther nathanwalther https://www.linkedin.com/in/nwalther/ code prReview
Jordan_Moore cricket007 https://www.linkedin.com/in/jordanmoorerhit/ code answers
Carlos_Cordero dccarlos https://www.linkedin.com/in/carlos-d%C3%A1vila-cordero-71128a11b/ code
Ishan_Dikshit ishandikshit https://www.linkedin.com/in/ishan-dikshit-4a1753ba/ code doc
Vinayak_Ponangi vinayakponangi https://www.linkedin.com/in/preethi-vinayak-ponangi-90ba3824/ code talks design prReview
Prabhakaran_Thatchinamoorthy prabhakar1983 https://www.linkedin.com/in/prabhakaranthatchinamoorthy/ code design
Rui_Zhang ruizhang0519 https://www.linkedin.com/in/rui-zhang-54667a82/ code
Miguel_Lucero mlucero10 https://www.linkedin.com/in/miguellucero/ code answers
René_X_Parra neoword https://www.linkedin.com/in/reneparra/ code doc blogpost talks design prReview
Contributors END -->
<!-- Contributors table START -->
| [<img src="https://avatars.githubusercontent.com/westeras?s=100" width="100" alt="Adam Westerman" /><br /><sub>Adam Westerman</sub>](https://www.linkedin.com/in/adam-westerman/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=westeras) | [<img src="https://avatars.githubusercontent.com/arunvasudevan?s=100" width="100" alt="Arun Vasudevan" /><br /><sub>Arun Vasudevan</sub>](https://www.linkedin.com/in/arun-vasudevan-55117368/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=arunvasudevan) 🎨 | [<img src="https://avatars.githubusercontent.com/nathanwalther?s=100" width="100" alt="Nathan Walther" /><br /><sub>Nathan Walther</sub>](https://www.linkedin.com/in/nwalther/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=nathanwalther) 👀 | [<img src="https://avatars.githubusercontent.com/cricket007?s=100" width="100" alt="Jordan Moore" /><br /><sub>Jordan Moore</sub>](https://www.linkedin.com/in/jordanmoorerhit/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=cricket007) 💁 | [<img src="https://avatars.githubusercontent.com/dccarlos?s=100" width="100" alt="Carlos Cordero" /><br /><sub>Carlos Cordero</sub>](https://www.linkedin.com/in/carlos-d%C3%A1vila-cordero-71128a11b/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=dccarlos) | [<img src="https://avatars.githubusercontent.com/ishandikshit?s=100" width="100" alt="Ishan Dikshit" /><br /><sub>Ishan Dikshit</sub>](https://www.linkedin.com/in/ishan-dikshit-4a1753ba/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=ishandikshit) [📖](git@github.com:homeaway/stream-registry/commits?author=ishandikshit) | [<img src="https://avatars.githubusercontent.com/vinayakponangi?s=100" width="100" alt="Vinayak Ponangi" /><br /><sub>Vinayak Ponangi</sub>](https://www.linkedin.com/in/preethi-vinayak-ponangi-90ba3824/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=vinayakponangi) 📢 🎨 👀 |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |

| [<img src="https://avatars.githubusercontent.com/prabhakar1983?s=100" width="100" alt="Prabhakaran Thatchinamoorthy" /><br /><sub>Prabhakaran Thatchinamoorthy</sub>](https://www.linkedin.com/in/prabhakaranthatchinamoorthy/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=prabhakar1983) 🎨 | [<img src="https://avatars.githubusercontent.com/ruizhang0519?s=100" width="100" alt="Rui Zhang" /><br /><sub>Rui Zhang</sub>](https://www.linkedin.com/in/rui-zhang-54667a82/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=ruizhang0519) | [<img src="https://avatars.githubusercontent.com/mlucero10?s=100" width="100" alt="Miguel Lucero" /><br /><sub>Miguel Lucero</sub>](https://www.linkedin.com/in/miguellucero/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=mlucero10) 💁 | [<img src="https://avatars.githubusercontent.com/neoword?s=100" width="100" alt="René X Parra" /><br /><sub>René X Parra</sub>](https://www.linkedin.com/in/reneparra/)<br />[💻](git@github.com:homeaway/stream-registry/commits?author=neoword) [📖](git@github.com:homeaway/stream-registry/commits?author=neoword) 📝 📢 🎨 👀 |
| :---: | :---: | :---: | :---: |
<!-- Contributors table END -->
This project follows the [all-contributors](https://github.com/kentcdodds/all-contributors) specification.
