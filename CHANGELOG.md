# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [0.4.1] - SNAPSHOT
### Changed
- Adding un-annotated `streamName` path param to streamUpsert HTTP resource (#69)
- Updated pom.xml to remove unused retrofit library, and clean up some versioning (#70)

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

[0.4.1]: https://github.com/HomeAway/stream-registry/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/HomeAway/stream-registry/compare/v0.3.2...v0.4.0
[0.3.2]: https://github.com/HomeAway/stream-registry/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/HomeAway/stream-registry/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/HomeAway/stream-registry/compare/v0.2.42...v0.3.0
