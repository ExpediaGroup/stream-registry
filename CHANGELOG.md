# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.4.5] - SNAPSHOT
### Changed
- Hardening check for ensuring KStream store is available during Integration Tests
- Populated the HealthCheck stream region from config file. Removed the dependency of MPAAS_REGION variable. (#103)

## [0.4.4] - 20190204
### Changed
- Jib now publishes docker image to dockerhub
- Fixed the failing HeathCheck (#101). Workaround for https://github.com/confluentinc/schema-registry/issues/894

## [0.4.3] - 20190131
### Changed
- Updated [Building locally](https://github.com/homeaway/stream-registry#building-locally) section in README with OpenJDK 11 reference
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

[0.4.5]: https://github.com/HomeAway/stream-registry/compare/v0.4.4...v0.4.5
[0.4.4]: https://github.com/HomeAway/stream-registry/compare/v0.4.3...v0.4.4
[0.4.3]: https://github.com/HomeAway/stream-registry/compare/v0.4.2...v0.4.3
[0.4.2]: https://github.com/HomeAway/stream-registry/compare/v0.4.1...v0.4.2
[0.4.1]: https://github.com/HomeAway/stream-registry/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/HomeAway/stream-registry/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/HomeAway/stream-registry/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/HomeAway/stream-registry/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/HomeAway/stream-registry/compare/v0.2.42...v0.3.0
