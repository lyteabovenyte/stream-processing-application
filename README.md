##### Building multi-container event-streaming application using Kafka platform and Kafka Streams

###### features:
- [**schema-registry**](https://docs.confluent.io/platform/current/schema-registry/index.html):
    - [x] [**Gradle Build Tool** and **Gradle plugins**](https://gradle.org) by [**schema-registry-plugin**](https://github.com/ImFlog/schema-registry-plugin) used for intracting with schema-registry for testing schema compatibility, registering schemas and configuring schema compatibility, [**gradle-avro-plugin**](https://github.com/davidmc24/gradle-avro-plugin) used for java code generation from Avro schema (.avsc) files, [**protobuf-gradle-plugin**](https://github.com/google/protobuf-gradle-plugin) used for java code generation from Protobuf schema (.proto) files and [**js2p-gradle**](https://github.com/eirnym/js2p-gradle) used for java code generation for schemas using the JSON schema specification.
    - [x] using different kinds of ***subject naming strategy*** and ***schema referencing*** along the schemas ( contains restriction of events in topics (De)serialization ) + registering via Gradle Schema registry plugin on *build.gradle* file in streams module.
    &nbsp;

- **Clients and Clients APIs**:
    - [x] implementing *custom **partition** method* of the Partitioner class 
    - [x] multiple related event types *in-order* through same topic partition using both *Avro* and *protobuf* schemas(examine this when you have multiple event types but closely related and being in-order is essential such as user-login, search, purchase flow which can provide useful trend.) + considering *typeCase* and *specific* objects of Avro and protobuf.
    - [x] ***Admin API*** to manage topics, partitions and records programmatically .
    - [x] transactional producer and idemptence producer to guratantee exactly-once semantic + consuming with *isolation.level=read_committed*.
    - [x] using ```sendOffsetsToTransaction``` method of the producer to commit offset on *__consumer_offset topic* using *producer* after processing record in consume-transform-produce cycle, to ensure committing after processing-producing and preventing non-processed record commits.
    &nbsp;

- [**Kafka Connect**](https://docs.confluent.io/platform/current/connect/index.html):
    - [x] the key concepts and relation between *kafka connect cluster* and *workers* with *tasks* that they do individually
    - [x] using some provided SMTs --> ```ValueToKey, ExtractField, MaskField``` and writing custom SMT (*and I love customs*)
    - [x] providing custom record headers to DLQ (dead-letter-queue), log errors for failed record while sinking to provide headers for more exploration on the cause of the error.
    - [x] working with ```Connector``` and ```Task``` interface. specifically extending abstract class ```SourceConnector```  which extends the ```Connector``` class to implement our **custom connector** and config our task using our connector with ```ConfigDef``` instance to define the configurations
    - [x] difference between connector instance and connector plugins:
        - Connectors in Kafka Connect define where data should be copied to and from.
        A **connector instance** is a logical job that is responsible for managing the copying of data between Kafka and another system.
        All of the classes that implement or are used by a connector are defined in a **connector plugin**.
        Both connector instances and connector plugins may be referred to as “connectors”,
        but it should always be clear from the context which is being referred to.
    - [x] using **monitorThread** to reconfigure connector's task whenever it notices changes on the sourceConnector symbols using ```connectorContext```
    - [x] creating **custom transformation** by implementing ```transformation``` interface, specially the ```apply``` method of the interface.
    - [ ] 