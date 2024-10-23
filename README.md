##### Building multi-container event-streaming application using Kafka platform and Kafka Streams

###### features:
- schema-registry related features:
    - [x] [**Gradle Build Tool** and **Gradle plugins**](https://gradle.org) by [**schema-registry-plugin**](https://github.com/ImFlog/schema-registry-plugin) used for intracting with schema-registry for testing schema compatibility, registering schemas and configuring schema compatibility, [**gradle-avro-plugin**](https://github.com/davidmc24/gradle-avro-plugin) used for java code generation from Avro schema (.avsc) files, [**protobuf-gradle-plugin**](https://github.com/google/protobuf-gradle-plugin) used for java code generation from Protobuf schema (.proto) files and [**js2p-gradle**](https://github.com/eirnym/js2p-gradle) used for java code generation for schemas using the JSON schema specification.
    - [x] using different kind of ***subject naming strategy*** and ***schema referencing*** along the schemas ( contains restriction of events in topics (De)serialization ) + registering via Gradle Schema registry plugin on *build.gradle* file in streams module.
- Clients:
    - [ ] 