# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
* New GraphQL API.
* New internal model with service, handler and repository layers. 
* Domain, Schema & Stream entities.
* Reference GraphQL client.

### Changed
- 1.0.0-SNAPSHOT -> 0.7.0-SNAPSHOT
- Changed groupId to `com.expediagroup.streamplatform`
- Changed packages to `com.expediagroup.streamplatform.streamregistry`
- Modularized - core, model, service-api/impl, repository-api/impl, graphql, app
- Removed rest API
- Removed consumer/producer service (to be reintroduced later)
- Removed Dropwizard
- Added Spring Boot
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

## [0.5.0] - 20190412
### Added
- Add health check to verify whether the underlying topic (used as Datastore) 
  is compaction enabled to make sure Stream Metadata is not lost. (#144)

### Changed
- Migrated version of jib to 1.0.2 (#151)

## [0.4.10] - 20190403
### Changed
- Fixed Stream Validation signature and updated implementation (#117)
- Fixed a NPE in get clusters(#145)
- Migrated KafkaManager to InfraManager (fixes #109) (#149)

### Removed
- Deleted KafkaManager and corresponding test (#149)

## [0.4.9] - 20190321
### Changed
- Updated swagger example values (#139)
- Extract out service logic from StreamDao to a new Service Layer (#140)
- Fixed Exception Handling to catch Runtime Exception in Cluster Resource (#141)
- Fixed the response type to text in Stream Resource (#141)
- Upgrade confluent version from 3.3.1 to 4.1.3 (#142)
- Fixing StreamRegistryHealthCheck for release/cut (#143)

## [0.4.8] - 20190308
### Added
- Added clusters Api (#131, #36) 

### Changed
- Updated license info to extend until Present (#138)

## [0.4.7] - 20190305
### Added
- Created `StreamRegistryManagedContainer` which will be responsible for ordering lifecycle for infra components (#124)
- Added support for registering Jersey Filters via config.yaml (#136)

### Changed
- Added `@NonNull` constraint check for `InfraManagerConfig.config` (#128)
- Re-ordered bean instantiation in `StreamRegistryApplication.run()` to guarantee order of start/stop (#124)
- Refactored the Exception chaining and removed business object from the custom Exception classes (#129)

## [0.4.6] - 20190209
### Changed
- Fixed healthcheck order of operations (#120)

## [0.4.5] - 20190208
### Changed
- Populated the HealthCheck stream region from config file. Removed the dependency of MPAAS_REGION variable. (#103)
- Refactored the Exception handling workflow in order to better communicate the actual error to customers. (#111)
- Hardening check for ensuring KStream store is available during Integration Tests. (#102)

## [0.4.4] - 20190204
### Changed
- Jib now publishes docker image to dockerhub
- Fixed the failing HeathCheck (#101). Workaround for https://github.com/confluentinc/schema-registry/issues/894

## [0.4.3] - 20190131
### Changed
- Updated [Building locally](https://github.com/ExpediaGroup/stream-registry#building-locally) section in README with OpenJDK 11 reference
- Fixed the failing HealthCheck. Passed a valid schema while creating HealthCheckStream (#94)

## [0.4.2] - 20190118
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

## [0.4.1] - 20190102
### Added
- Docker build commands and documentation (#74)

### Changed
- Adding un-annotated `streamName` path param to streamUpsert HTTP resource (#69)
- Updated pom.xml to remove unused retrofit library, and clean up some versioning (#70)
- Updated pom.xml to fix `make run` goal (#76)

### Removed
- Deleted TODO/Documentation that referenced incorrect `http://localhost:8081/healthcheck` (#64)

## [0.4.0] - 20181218
### Added
- Schema validation support via `SchemaManager` interface with default Confluent implementation provided (#41)

## [0.3.2] - 20181216
### Changed
- Updated README to something that outlines this a bit better. (#54)
- Changed .travis.yml and added a setup script to securely send credentials for sigining and deploying master builds. (#57)
- Added appropriate variables so that all encrypted keys work. (#57)

## [0.3.1] - 20181212
### Changed
- Specified `deploy` goal in maven-release-plugin
- Fixed "site breaking release process" (#47)
- Fixed ossrh release requirements  (#50)
- DummyForJavaDoc.java in assembly module to generate javadoc (#51)

## [0.3.0] - 20181211
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
