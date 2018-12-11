# Usage

Required Local Environment
---
The local 'dev' version of stream registry requires a running version of Apache Kafka
and Confluent's Stream Registry running locally on port 9092 and 8081 respectively.

A quick version of this is to download, install and start
[Confluent CLI](https://docs.confluent.io/current/cli/index.html).

Build stream-registry
---
```
make build
```

Start stream-registry
---
```
make run
```

Check that your application is running at `http://localhost:8080/swagger`

Run Unit Tests
---
```
make tests
```
