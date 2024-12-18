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
- [**Kafka Streams**](https://kafka.apache.org/20/javadoc/org/apache/kafka/streams/kstream/KStream):
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
        -  every processor's state store is backed by a changelog for state recvoery, just like any other databases that uses changelog for state recovery.
        -  covering usage of the "**standby tasks**" for faster failover when an KStream application drops using `num.standby.replica` configuration.
        -  as the tasks are in shared-nothing architecture, there is no locking or concurrency issues around the state stores.
        -  each task is the sole owner of the assigned store and is the only one to read and write to the store.
        -  kafka streams configuration objects for naming state stores and repartition topics:
            | conf object        |            what's named           |    where used          |
            |------------------- |-----------------------------------|-------------------------|
            | `Materialized`       | State stores, changelog topic     | `Aggregatin`             |
            |``Repartitioned``     | Repartition topic                 | `Repartition`(manual by user)|
            |`Grouped`             | Repartition topic                 | `groupBy`(automatic repartitioning)|
            | `StreamJoined`       | State stores                      | `Join` (automatic repartitioning)|
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

- [**KTables**](https://kafka.apache.org/26/javadoc/org/apache/kafka/streams/kstream/KTable.html):
    - intro:
        - we can imagine the stream event as a log and the stream of updates as a changelog. KTables are update stream or somehow changelog with a compact nature internally.
        - KTables are stateful.
        - an overloaded version of `StreamBuilder.table` also accept a ``Materialized` instance allowing us to configure the type of store and provide naming for querying the table
        - how the updating of the record (aka. **KTable aggregation**) is applied in two-step process:
            - 1. the old value gets subtracted.
            - 2. the new value gets added.
          - so KTable sends both the old and new value from a key to the aggregator's processor, and aggregator processor remove the old value based on the received key and adds the new value.
          - and also for other KTable's API methods such as reduce and count, we do the same process as we only have to save the newly added value, we first substract the old one and add the new one to the cumulated variable.
        - as the KTable is partitioned among the KStream application with the same application ID, each KTable only contains it's own partition's data, but there is another concept in KTable called `GlobalkTable` which consume all the topic partition's data.
        - 
    - features have been covered:
        - udating KTable or update stream to KStream using `toStream()` method
        - usage of `KStream.toTable` and `KTable.toStream` for changing one to another.
        - with the `GlobalKTable` application, KStream don't create a changelog topic, as the source topic would act as a changelog for failover recovery. so condiser using `GlobalKTable` for **broadcasting information** between kafka Streams application for use in joins.
        - another andvantage of `GlobalKTable` is that since it has all partition's data, we can use the value of a given Key for joining using a `KeyValueMapper`.
        - joining KStream with KTable need **co-parititoning**, so each key is on the same parititon for both the KStream and KTable thread to join. if the quantity of partitions of the source topics is different, repartitioning is needed.
        - is a *KStream-KTable join*, only when the KStream updates a key, `ValueJoiner` interface yield a new result of join, but updating a key in KTable only results updating a key in the KTable and no result is yileded from join.
        - the usage of `store.PersistetVersionedKeyValueStore` we can version the table using timestamp and with a **versioned KTable** the join for a record in the KStream uses the same timestamp range from the table resulting in a *temporal* correct result.
        - is KStream-GlobalKTable joins --> we provide three parameter to the join:
            - 1. the GloablKTable to join the Stream with
            - 2. a KeyValueMapper to extract the key in the Stream ( which could be `null` and we should choose one of the fields for the key), for the GlobalKTable to join with.
            - 3. the `ValueJoiner` interface to compute the join result.
        - a good rule of thumb in joining KStream, KTable and GlobalKTables is --> use KStream-GlobalKTable joins only when you have static lookup data to join with a stream and use KStream-KTable joins for dynamic streams and when the stream is extensive, cause Kafka Streams will distribute it across multiple instances.
- **Windowing and Timestamps**:
    - **windowing**:
        - intro:
            - using window or windowing means that putting aggregations data into discrete time bucket. (aka. put the data into a specific **context**.)
            - while both KStream and KTable supprot aggregation, windowing is available on KStream API, due to the nature of their abstractaion.
            - timestamps are the key to windowing and they are the drivers of the action.
            - types of windowing:
              | type              | method              | feature          |
              | ----------------- | --------------------|------------------|
              | hopping windowing | windowedBy(TimeWindows.[...]).advandeBy(...) + aggregator | overlapping windows and refresh the aggregation by advandeBy second |
              | tumbling windowing | windowedBy(TimeWindows.[...]) + aggregator | it's a hopping windowing but the advandeBy time is the same as the windowedBy |
              | session windowing |  windowedBy(SessionWindows.[...]) + aggregator and sessionMerger| sessions will continue to grow in size until there's a gap that exceeds the configured amount of time |
              | sliding windowing | windowedBy(SlidingWindows.[...]) + aggregator| sliding windows provide a continuous view of changes in events and only include events occuring within a specified timeframe in a fixed window size |

            - sliding windows starting and ending time are inclusive and look back for the events occuring within the defined time difference.
            - sliding window only evaluate aggreate when a new record comes into the topic or a record falls out. so kafka streams only evaluates it when the content of the window changed, so it prevent redundancy.
            - with hopping and tumbling windowing strategy, the event's timestamp determine which window it is belong to. cause it these strategies, the **windows align to the epoch**.
            - 

        - related features:
            - the `GroupByKey` method returns `KGroupedStream` which it's API includes all the method needed for windowing, so after that we could call `windowedBy` method and provide it's single parameter, the window instance for the aggregation. --> such as "`TimeWindows`" class.
            - windowed aggregation wrap the key in a `Windowed` class containing the key and the window of the aggregation.
            - with the session window aggregation, we need a `Merger` instance to merge the results of the primary aggregation ( that is specified in topology) with another aggregation to combine the primary result and provide a single agg result for each session.
            - when we specify a windowed aggregation, kafka streams wraps the key with a `Windowed` object which include the start and the end of the window.
            - `Grace` is a way in kafka streams to allow and **out-of-order** record to join a window even though it arrived late due network partition or etc.
            - as we have two strategy to derive a **final result** from the window aggregation:
                - 1. `EmitStrategy` --> which emits the final result after the window has been closed and no more intermediate result are derived. ( `EmitStrategy.onWindowClosed()` or `EmitStrategy.onWindowUpdate()`, the latter one is the default behavior. )
                - 2. `KTable.suppress` --> suppression works by buto ffering the intermediate results, per key, until the window closes and there is a final result.
            - the suppresion approach on retreiving a final result comes with two `BufferConfig` option:
                - 1. *eager buffering*: would emit a result if the buffer configuration met. the possibility of getting multiple result per window aggregation. 
                - 2. *strict buffering*: would'nt emit result until the window is not closed. the possibility of OOM(OutOfMemory exception) if the buffering and available heap is not considered.
                - it worth mentioning that with the "strict buffering" approach you can define the buffer size either to be `unbounded()` or `maxRecords(x).shutDownWhenFull()`
                - the trade-off is something between duplicates vs shut-down gracefully.
                - with the eager approach we can set the buffer size as well as setting the time to wait for new record until we move the result downstream.
        
    - **timestamp**:
        - intro:
            - in the meantime, timestamps in stream processing in general can be grouped into <u>three</u> main categories:
                - 1. event time ( timestamp set by the produce or **event-time processing semantic**. )
                - 2. ingestion time ( timestamp set by the broker or **log append-time processing semantic**. `LogAppendTime`)
                - 3. processing time
        - related features:
            - choosing timestamp semantics using `TimestampExtractor`
            - `ExtractRecordMetadataTimestamp` is an abstract class that provides the core functionality for extracting the metadata form the `ConsumerRecord`.

- **Processor API**:
    - intro:
        - the main difference between KStream and Processor API is their return type. with KStream the return type is either a KStream or a KTable and with Processor API's topolgy, the return type is the <u>same</u> Processor API topolgy.
        - a key consideration for using Processor API over KStream to create topology with more control over the source, porcessor and sink node is the restriction of commit time and cache flushing which KStream provides and with Processor API, it could be much more control over the records via the `Processor.process()` method.
        - 
    - features:
        - using `Topolgy.topolgy` type to `addSource()` and `addProcessor()` with required parameters.
        - by using `ContextualProcessor` which is a class which provides override for `Processor` interface and by using `Processor.porcess()` method of the child processor and using `Context()` we can forward individual records to intended sink topics.
        - the `Processor` interface provides the `init()`, `process()` and `close()` methods. the `Processor` is the primary driver of any application logic that works with records in our streaming application.
        - using `ProcessorSupplier` interface which provides a `stores` method that will automatically wire up the processor with any `StoreBuilder` instance you provide.
        - using `ProcessorSupplier.stores` method, we have a way to automatically wire up `StateStore` instances to processors.
        - with processor's `init()` method, we can config and initialize some of the functionality to the topology's processor along the way, such as detemining and scheduling when to emit each record instead of forwarding them right after when they arrive, or initialize a state store for the processors.
        - `punctuator` is an interface that's a callback to handle the scheduled execution of processor logic but is encapsulated in the `Punctuator.punctuate`.
        - some sort of **data-driven aggregation** where we want to aggregate data in a given window on some sort of criteria, whenever some certain condition happened we start measuring the aggregation in a pre-set window and then aggregate until an incoming record that no longer meets the criteria and condition.