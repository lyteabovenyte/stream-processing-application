# Kafka Streams

## feature covered:
There's a README file in each chapter directory describing the code located there and instructions required.

* Schema Registry producer and consumer clients for working with Avro, Protobuf, or JSON Schema
* `KafkaProducer` and `KafkaConsumer` client examples
* Intro to Kafka Streams
* Stateful operations in Kafka Streams
* The `KTable` API example code
* Windowing examples
* Kafka Streams Processor API examples
* ksqlDB example SQL files
* Example code written in support of demonstrating testing

## Supporting code

The following code is used throughout the repo as helper code.

* [clients](src/main/java/) - `MockDataProducer` and the `ConsumerRecordsHandler` interface
* [serializers](src/main/java/) (de)serializer implementations and helper classes for JSON and Protobuf records used throughout the examples
* [data](src/main/java/) Utility code used to generate data for the examples
* [utils](src/main/java/) Utility classes used for topic management, serde creation, integration testing utilities etc.
* [BaseStreamsApplication](src/main/java/) - An abstract class containing some common functionality for all Kafka Streams examples developed.


