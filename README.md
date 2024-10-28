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
- [**Kafka Streams**](https://kafka.apache.org/documentation/streams/):
    - intro:
        -  *Kafka streams* is a graph of processing nodes transforming evnet data as it streams through each node. so let's get familiar with *kafka streams API*
        -  kafka streams is an abstraction on top of kafka producer and consumer client API, as it is the native stream processing library for apache kafka, it does not run inside the cluster or the broker but it connects as a client application.
        -  kafka streams approach:
              -  1. Defining configurations items
              -  2. Creating Serde instance, either custom or predefined, used in the (De)serialization of records. (**Serde** is a wrapper object that contain a serializer and a deserializer for a given type)
              -  3. Building processor topology
              -  4. Creating and starting kafka streams
        - **processor topology** is merely a logical abstraction for your stream processing code, and contain one or more processor nodes (but typically they are logicaly one processor topology).
        - **Branching** (aka. **Spliting**) provides an elegent way to process record differently whithn the same stream. `KStream#split` returns a BranchedKstream object which can be used with a `predicate` interface containing a `test` method + the branch which act as the gate for the branched record.
        - differences between `Branched.withConsumer` and `Branched.withFunction`
    &nbsp;
    - features that have been expermineted:
        - [x] Produced configuration in sink processor which contains custom ```StreamPartitioner```.
        - [x] differences between `Produced` and `Consumed` instances and their configurations
        - [x] expermineting ```ValueMapper(V, V1)``` interface and the method ```ValueMapper.apply``` to implement a child processior in DAG. also expermineting other similar mappers --> `KeyValueMapper` and `ValueMapperWithKeys` in processor nodes depending on the condition.
        - [x] considering the usage of ```flatMap```, a well-known operation from functional programming which emits zero or more records from a single input record by falttening a collection returned from a `KeyValueMapper` or `ValueMapper`.
        - [x] considering customSerde and confuguring them.
        - [x] expermineting `KStream#filter` and `KStream#split` and for the latter, examine `BranchedKStream` object with two parameters called `predicate` and `branched`.
        - [x] the handy methods of split and the `Branched` object --> `branched.as` which just get the predicate and the output topic and `branched.withFunction` which can get a `mapValue` lambda funciton for SMT latter of branching and splitting and the output topic
        - [x]  overloaded `Named` and `withName` method of *Streams DSL* and *consumed* and *produced* operations for better naming in topolgy description. as the naming becomes critical where the state is involved
        - [x] using `TopicNameExtractor` which provide just one method named `extract` for dynamic routing of messages.

        &nbsp;
- **Stateful operations within kafka streams**:
    - intro:
        -  group by key is a prerequisite for stateful aggregation.
        -  the result of all aggregation operation in kafka is **KTable**, so we should use `toStream` method to convert it to *KStream*.
        -  as the `reduce` returns a result with the same type, if you want to change the type of the result, you can use `aggregate`.
        -  covering the *cache layer* in KStream, that is used to write just the updated and last record for each key to the changelog and state store.
        -  experminting on repartitioning, and how kafka streams adds a sink and a source node to the topology to cover repartitioning the records.
        -  Join operation internals, containing state store for each topic and a `ValueJoiner` instance which it's `apply` method does the actual joining and passes the produced record to the next processor.
        -  
    &nbsp;

    - features covered:
        - [x] covered `GroupByKey` method and the return type, `KGroupedStream.KGroupedStream` which provides method `aggregate`, `count` and `reduce`.
        - [x] repatitioning is done when kafka streams notices:
            - 1. an operation where the keys have changed
            - 2. a downstream opertion which depends on the key. (such as ``GroupByKey` or an aggregation or join).
        - [x] `KStream.repartition` method which accept one parameter, `Repartitioned`, it allows us to specify:
            - 1. the Serdes fo the key and value
            - 2. the base name for the topic
            - 3. the number of partitions to use for the topic
            - 4. a `StreamPartitioner` instance, should you need to customize the distribution of records to parititons.
        - [x] using optimization configuration in kafka streams config to reduce the redundant repartitioning nodes using the underlying processor graph that kafka streams is building under the hood.
        - [x] Join and `ValueJoiner.apply` method which gets three possible parameters <V1, V2, R>, the first two parameter are the value types for join and "R" is the result type after join.
        - [x] experimenting `JoinWindows` configuration object and `JoinWindows.before` and `JoinWindows.after` configuration methods.
        - [ ] 
