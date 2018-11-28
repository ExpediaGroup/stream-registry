# stream-registry

Required Local Environment
---
The local 'dev' version of stream registry requires a running version of Apache Kafka
and Confluent's Stream Registry running locally on port 9092 and 8081 respectively.

A quick version of this is to download, install and start 
[Confluent CLI](https://docs.confluent.io/current/cli/index.html).

Build stream-registry
---
```
./stream-registry.sh build
```


Start stream-registry
---
```
./stream-registry.sh run
```

* check that your application is running enter url `http://localhost:8080/swagger`

Run Unit Test , and Jacoco Code Coverage.
---
`mvn clean verify`

The test coverage report is available at `./target/site/jacoco/index.html`

Health Check
---
To see your applications health enter url `http://localhost:8081/healthcheck`
