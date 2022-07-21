# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [1.0.3] 2022-07-21
### Updated
- Now blocking Stream deletes if there are Processes that depend on it.

## [1.0.2] 2022-07-20
### Added
- Added delete functionality for Domains

## [1.0.1] 2022-06-22
### Added
- Added option to add metrics to `EntityView`

## [1.0.0] 2022-06-20
### Changed
- Entity key values are no longer normalised by trimming whitespace and converting to lowercase. Entity key values are
  now case-sensitive and by default are validated as being in snake case (lowercase) when an entity is created. If you
  wish to customise entity key value validation please implement a `KeyValidator` component for each key type that 
  requires a different validation algorithm.
### Updated
- Now using Spring Boot version 2.7.0.

## [0.20.6] 2022-01-12
### Added
- added arbitrary property support for repository kafka event receiver/sender

## [0.20.5] 2022-01-07
### Updated
- updated all log4j dependencies to mitigate log4shell vulnerability

## [0.20.4] 2021-11-12
### Added
- Added CRUD Security for Process
### Updated
- Updating versions of Confluent/Avro to align with downstream implementation

## [0.20.3] 2021-09-13
### Fixed
- Now properly defaulting function field to empty string when converting from 
  model to state model to preserve backwards compatibility.

## [0.20.2] - 2021-09-09
### Fixed
- Added function to kafka -> graphql conversion

## [0.20.1] - 2021-09-09
### Added
- Added `function` field to all Specification types
### Bugfix
- `DefaultEntityView` now based on `ConcurrentHashMap`

## [0.20.0] - 2021-08-04
### Added
- Added Model/State/Services for new Process and ProcessBinding Entities

## [0.19.1] - 2021-04-26
### Added
- Implemented entity delete functionality.
- Added `security` to Specification models.

### Changed
- Added `security` to AvroSpecification and AvroStreamSpecification schemas, defaulting to empty map.

## [0.17.0] - 2021-01-29
### Changed
- Enhanced `EntityView` to support the retrieval of deleted `Entity`s

## [0.16.0] - 2021-01-25
### Changed
- `ApolloExecutor` returns a `ApolloResponseException` with access to the underlying `Response` on errors.
- Downgraded to Java 8 to increase support within the wider streaming ecosystem.

## [0.15.1] - 2020-11-16
### Changed
- cli: On cascading stream delete, don't delete schemas that are being used by other streams.

## [0.15.0] - 2020-11-11
### Added
- Authorisation for read, findAll, and find operations for Consumer, ConsumerBinding,
  Domain, Infrastructure, Producer, ProducerBinding, Schema, Stream, StreamBinding, Zone

### Changed
- Refactored upsert calls in mutations and removed upsert methods from services
- Refactored update_status calls

### Removed
- Removed implementation of SecurityAuthoriser based authorisation

## [0.14.5] - 2020-10-19
### Added
- Authorisation for create, update, delete, and updateStatus operations for Consumer, ConsumerBinding,
  Domain, Infrastructure, Producer, ProducerBinding, Schema, Stream, StreamBinding, Zone

### Changed
- Refactored updateStatus for Consumer, ConsumerBinding, Domain, Infrastructure, Producer,
  ProducerBinding, Schema, Stream, StreamBinding, Zone

## [0.14.4] - 2020-10-05
### Added
- Access control setup to handle who can perform mutations on GraphQL.

## [0.14.3] - 2020-10-05
### Changed
- Enhanced support for authentication - cli now supports streamRegistryUsername and streamRegistryPassword

## [0.14.2] - 2020-09-29
### Added
- Added CLI command to discover and delete all child entities of a stream.

## [0.14.1] - 2020-09-25
### Added
- Added custom configurer to `DefaultApolloClientFactory` customise okhttp client.

## [0.14.0] - 2020-07-29
### Added
- Added CLI supporting creation and deletion of entities.
- Added authentication support for state api

### Bugfix
- Fixed erroneous tag filter.
- Fixed missing filter for name in Producer and Consumer Binding queries.

### Removed
- Removed postgres repository.

## [0.13.0] - 2020-07-17
### Changed
- Changed `Event.of` factory methods to more descriptive names and return the actual type.
- Removed notification support.

## [0.12.3] - 2020-06-24
### Bugfix
- Fixed error in `KafkaEventReceiver`.

## [0.12.2] - 2020-06-24 [YANKED]

## [0.12.1] - 2020-05-29
### Bugfix
- Fixed off by one error in `KafkaEventReceiver`.

## [0.12.0] - 2020-05-27
### Added
- Kafka Repository implementation.

## [0.11.1] - 2020-05-27
### Added
- Added logging to Kafka and Apollo state code.

### Changed
- Moved graphql modules under graphql directory.
- Bumped graphql-java versions to latest.
- Fix a bug in state Avro conversion of stream entities.
- Enhanced `EntityViewListener` to show nullability of parameters to give more control to Kotlin consumers.

### Removed
- graphql-client, graphql-client-reactor & graphql-client-support modules.

## [0.11.0] - 2020-05-27
### Added
- Added state library.

## [0.10.6] - 2020-05-21
### Added
- Emit event on producer/consumer mutation.
- Emit event on streambinding mutation.
- Added `repository-api` module to define new repository abstraction exposing model instead of data.
- Added `repository-postgres` module to implement new repository adapting JPA to consolidate JPA/Hibernate implementation details.

### Changed
- Split internal representation into Model and Data
- Fixed issues with Stream updates
- Addressed possible issue with notifications

### Bugfix
- Fixed issues with referential integrity on binding entities.

## [0.10.5] 2020-03-24
### Changed
- Removed auto configuration of default handlers.

### Added
- A listener for streamBinding events
- A Kafka handler for streamBinding events
- A listener for producer events
- A Kafka handler for producer events

## [0.10.4] 2020-03-20
### Added
- Instrumented Resolvers with timers in line with queries and mutations
- Enabled hibernate metrics by default

### Changed
- A new field was added for `AvroStream record` in stream-registry-notification schema
- Unit tests were modified to verify and test the new field
- Utilise JPA `Example` to improve the performance of search operations in GraphQL Resolvers.

## [0.10.3] 2020-03-17
### Added
- Cache hints

## [0.10.2] 2020-03-16
### Added
- Hibernate caching

### Changed
- Bugfix - Can now Query Streams by their schema

## [0.10.1] 2020-03-02
### Added
- A stream event handler for Kafka
- Unit and integration tests for stream event handler
- New config classes due a refactoring of `NotificationEventConfig`
- New config properties for stream custom parser methods

### Changed
- `NotificationEventConfig` was refactored

## [0.10.0] 2020-03-02
### Added
- Added a `notification-support` with a generic model for event listeners and handlers, including
    - A listener for schema events
    - A listener for stream events
    - A Kafka handler for schema events
    - An Automatic Kafka setup
    - A default avro protocol (a new one can be configured and dynamically loaded)
    - Integration tests
- Added a spring application event multicaster
- Added an interface for services to multicast CUD events
- Unit tests

### Changed
- Changed all core service to support `NotificationEventEmitter`
- Changed all core service CUD methods to emit notification events
- Configuration in `spring-boot-starter` module to include `notification-support` config

### Bugfix
- Status field will return blank object when it is null for all the entities.
- updateStream contract change(SchemaKeyInput not required in input) and upsertStream fixes for no schema during create

## [0.9.4] - 2020-02-06
### Added
- Added an `authentication_group` tag to the `graphql_api` metric.

## [0.9.3] - 2020-01-20
### Bugfix
- Also fixed incorrectly wired field in `ConsumerBindingResolver`.

## [0.9.2] - 2020-01-20
### Bugfix
- Fixed incorrectly wired field in `ProducerBindingResolver`.

## [0.9.1] - 2020-01-13
### Added
- Added StreamRegistryApolloClient.builder to create client with required adapters.

### Changed
- Changed `NameNormaliser` valid pattern.

## [0.9.0] - 2019-12-06
### Added
- Setup for flyway db migration tool and initial db script.
### Changed
- Turning off auto-generation of db in integration.
- `byKey` query result are now optional.

## [0.8.2] - 2019-11-26
### Changed
- Only default handlers present by default.

## [0.8.1] - 2019-11-22
### Added
- Simple validation rules for `EgspKafkaStreamHandler`.
- Added schema to client query return of stream.
- Added optional Binding field to Producer and Consumer entities.

## [0.8.0] - 2019-11-07
### Added
- Refactor of all entity structures and implementation of CRUD operations.
- Added metrics to all GraphQL api methods.

### Changed
- Switch to Spring's `CrudRepository` for the backend.

## [0.7.1] - 2019-08-20
### Added
- Added Zone, Infrastructure, Producer, Consumer, StreamBinding, ProducerBinding & ConsumerBinding entity types to the API.

## [0.7.0] - 2019-08-15
### Added
- New GraphQL API.
- New internal model with service, handler and repository layers.
- Domain, Schema & Stream entities.
- Reference GraphQL client.
- Docker pull and push repo url.

### Changed
- Converted project into spring boot starter
- 1.0.0-SNAPSHOT -> 0.7.0-SNAPSHOT
- Changed groupId to `com.expediagroup.streamplatform`
- Changed packages to `com.expediagroup.streamplatform.streamregistry`
- Modularized - core, model, service-api/impl, repository-api/impl, graphql, app
- Removed rest API
- Removed consumer/producer service (to be reintroduced later)
- Removed Dropwizard
- Added Spring Boot
- Docker image accepts environment variables for Spring Configuration
- Removed charts (to be reintroduced later)
- Removed health checks (to be reintroduced later)
- Removed remaining unused files
- Removed integration tests (a small number to be reintroduced later)
- Replaced ConfluentSchemaManager with simple HTTP impl.
- Reformat 2 spaces indent
- Removed unused dependencies & plugins
- License update  

## [0.5.1] - SNAPSHOT
### Added
- Updated `README` with Expedia Group stream registry announcement (#155)
- Increasing the WAIT time to 100ms in test cases to allow the underlying datastore to sync with the commits.

### Changed
- Updated mkdocs.yml to `expediagroup` (#168)

## [0.5.0] - 2019-04-12
### Added
- Add health check to verify whether the underlying topic (used as Datastore)
  is compaction enabled to make sure Stream Metadata is not lost. (#144)

### Changed
- Migrated version of jib to 1.0.2 (#151)

## [0.4.10] - 2019-04-03
### Changed
- Fixed Stream Validation signature and updated implementation (#117)
- Fixed a NPE in get clusters(#145)
- Migrated KafkaManager to InfraManager (fixes #109) (#149)

### Removed
- Deleted KafkaManager and corresponding test (#149)

## [0.4.9] - 2019-03-21
### Changed
- Updated swagger example values (#139)
- Extract out service logic from StreamDao to a new Service Layer (#140)
- Fixed Exception Handling to catch Runtime Exception in Cluster Resource (#141)
- Fixed the response type to text in Stream Resource (#141)
- Upgrade confluent version from 3.3.1 to 4.1.3 (#142)
- Fixing StreamRegistryHealthCheck for release/cut (#143)

## [0.4.8] - 2019-03-08
### Added
- Added clusters Api (#131, #36)

### Changed
- Updated license info to extend until Present (#138)

## [0.4.7] - 2019-03-05
### Added
- Created `StreamRegistryManagedContainer` which will be responsible for ordering lifecycle for infra components (#124)
- Added support for registering Jersey Filters via config.yaml (#136)

### Changed
- Added `@NonNull` constraint check for `InfraManagerConfig.config` (#128)
- Re-ordered bean instantiation in `StreamRegistryApplication.run()` to guarantee order of start/stop (#124)
- Refactored the Exception chaining and removed business object from the custom Exception classes (#129)

## [0.4.6] - 2019-02-09
### Changed
- Fixed healthcheck order of operations (#120)

## [0.4.5] - 2019-02-08
### Changed
- Populated the HealthCheck stream region from config file. Removed the dependency of MPAAS_REGION variable. (#103)
- Refactored the Exception handling workflow in order to better communicate the actual error to customers. (#111)
- Hardening check for ensuring KStream store is available during Integration Tests. (#102)

## [0.4.4] - 2019-02-04
### Changed
- Jib now publishes docker image to dockerhub
- Fixed the failing HeathCheck (#101). Workaround for https://github.com/confluentinc/schema-registry/issues/894

## [0.4.3] - 2019-01-31
### Changed
- Updated [Building locally](https://github.com/ExpediaGroup/stream-registry#building-locally) section in README with OpenJDK 11 reference
- Fixed the failing HealthCheck. Passed a valid schema while creating HealthCheckStream (#94)

## [0.4.2] - 2019-01-18
### Added
- Architecture diagram (#4)
- Standalone `infra-provider` module (#78)
- Build and Runtime support for OpenJDK11

### Changed
- Stop Stream Creation for existing topics if topic configs don't match (#52)
- Now DELETE consumer/producer services remove only one client (not all preceding list before a client) (#73)
- Rename `kafka-infra-provider` to `infra-provider-kafka` (#78)
- `maven-enforcer-plugin` to enforce SLF4J logging (#82)
- Java packages moved from `com.homeaway.streamingplatform` to `com.homeaway.streamplatform.streamregistry`.

## [0.4.1] - 2019-01-02
### Added
- Docker build commands and documentation (#74)

### Changed
- Adding un-annotated `streamName` path param to streamUpsert HTTP resource (#69)
- Updated pom.xml to remove unused retrofit library, and clean up some versioning (#70)
- Updated pom.xml to fix `make run` goal (#76)

### Removed
- Deleted TODO/Documentation that referenced incorrect `http://localhost:8081/healthcheck` (#64)

## [0.4.0] - 2018-12-18
### Added
- Schema validation support via `SchemaManager` interface with default Confluent implementation provided (#41)

## [0.3.2] - 2018-12-16
### Changed
- Updated README to something that outlines this a bit better. (#54)
- Changed .travis.yml and added a setup script to securely send credentials for sigining and deploying master builds. (#57)
- Added appropriate variables so that all encrypted keys work. (#57)

## [0.3.1] - 2018-12-12
### Changed
- Specified `deploy` goal in maven-release-plugin
- Fixed "site breaking release process" (#47)
- Fixed ossrh release requirements  (#50)
- DummyForJavaDoc.java in assembly module to generate javadoc (#51)

## [0.3.0] - 2018-12-11
### Added
- Integration with travis-ci (#39)
- Makefile for build commands (#39, #43)

### Removed
- Shell script for build commands (#43)

[0.5.1]: https://github.com/ExpediaGroup/stream-registry/compare/v0.5.0...v0.5.1
[0.5.0]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.10...v0.5.0
[0.4.10]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.9...v0.4.10
[0.4.9]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.8...v0.4.9
[0.4.8]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.7...v0.4.8
[0.4.7]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.6...v0.4.7
[0.4.6]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.5...v0.4.6
[0.4.5]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.4...v0.4.5
[0.4.4]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.3...v0.4.4
[0.4.3]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.2...v0.4.3
[0.4.2]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/ExpediaGroup/stream-registry/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/ExpediaGroup/stream-registry/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/ExpediaGroup/stream-registry/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/ExpediaGroup/stream-registry/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/ExpediaGroup/stream-registry/compare/v0.2.42...v0.3.0
