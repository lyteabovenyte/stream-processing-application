package bbejeck.end-to-end;


import bbejeck.BaseStreamsApplication;
import bbejeck.end-to-end.processor.BeerPurchaseProcessor;
import bbejeck.end-to-end.processor.LoggingProcessor;
import bbejeck.Processor-API.proto.BeerPurchase;
import bbejeck.clients.MockDataProducer;
import bbejeck.utils.SerdeUtil;
import bbejeck.utils.Topics;
import net.datafaker.Faker;
import net.datafaker.providers.base.Number;
import net.datafaker.providers.food.Beer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.UsePartitionTimeOnInvalidTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.apache.kafka.streams.Topology.AutoOffsetReset.LATEST;

public class PopsHopsPrintingApplication extends BaseStreamsApplication {
    static final Logger LOG = LoggerFactory.getLogger(PopsHopsPrintingApplication.class);
    final static String INPUT_TOPIC = "beer-purchases";
    final static String INTERNATIONAL_OUTPUT_TOPIC = "international-sales";
    final static String DOMESTIC_OUTPUT_TOPIC = "domestic-sales";

    @Override
    public Topology topology(Properties streamProperties) {
        Serde<BeerPurchase> beerPurchaseSerde = SerdeUtil.protobufSerde(BeerPurchase.class);
        Deserializer<BeerPurchase> beerPurchaseDeserializer = beerPurchaseSerde.deserializer();
        Serde<String> stringSerde = Serdes.String();
        Deserializer<String> stringDeserializer = stringSerde.deserializer();
        Serializer<String> stringSerializer = stringSerde.serializer();
        Serializer<BeerPurchase> beerPurchaseSerializer = beerPurchaseSerde.serializer();
        Map<String, Double> conversionRates = Map.of("EURO", 1.1, "POUND", 1.31);

        Topology topology = new Topology();

        String domesticSalesSink = "domestic-beer-sales";
        String domesticPrintingProcessor = "domestic-printing";
        String internationalSalesSink = "international-beer-sales";
        String internationalPrintingProcessor =  "international-printing";
        String purchaseSourceNodeName = "beer-purchase-source";
        String purchaseProcessor = "purchase-processor";

        topology.addSource(LATEST,
                        purchaseSourceNodeName,
                        new UsePartitionTimeOnInvalidTimestamp(),
                        stringDeserializer,
                        beerPurchaseDeserializer,
                        INPUT_TOPIC)
                .addProcessor(purchaseProcessor,
                        () -> new BeerPurchaseProcessor(domesticPrintingProcessor,
                                                        internationalPrintingProcessor,
                                                        conversionRates),
                        purchaseSourceNodeName)
                .addProcessor(domesticPrintingProcessor,
                        () -> new LoggingProcessor<String, BeerPurchase, String, BeerPurchase>("Domestic-Sales:"),
                        purchaseProcessor)
                .addProcessor(internationalPrintingProcessor,
                        () -> new LoggingProcessor<String, BeerPurchase, String, BeerPurchase>("International-Sales:"),
                        purchaseProcessor)
                .addSink(internationalSalesSink,
                        INTERNATIONAL_OUTPUT_TOPIC,
                        stringSerializer,
                        beerPurchaseSerializer,
                        internationalPrintingProcessor)
                .addSink(domesticSalesSink,
                        DOMESTIC_OUTPUT_TOPIC,
                        stringSerializer,
                        beerPurchaseSerializer,
                        domesticPrintingProcessor);
        
        return topology;
    }

    public static void main(String[] args) throws Exception {
        PopsHopsPrintingApplication popsHopsApplication = new PopsHopsPrintingApplication();
        Topics.maybeDeleteThenCreate(PopsHopsPrintingApplication.INPUT_TOPIC,
                PopsHopsPrintingApplication.DOMESTIC_OUTPUT_TOPIC,
                PopsHopsPrintingApplication.INTERNATIONAL_OUTPUT_TOPIC);
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "pops-hops-printing-application");
        Topology topology = popsHopsApplication.topology(properties);
        Serializer<BeerPurchase> beerPurchaseSerializer = SerdeUtil.protobufSerde(BeerPurchase.class).serializer();
        try (KafkaStreams streams = new KafkaStreams(topology, properties);
             MockDataProducer mockDataProducer = new MockDataProducer()) {
            mockDataProducer.produceWithRecordSupplier(
                    beerPurchaseProducerRecordSupplier,
                    new StringSerializer(),
                    beerPurchaseSerializer
            );
            streams.start();
            LOG.info("PopsHops application started");
            CountDownLatch countDownLatch = new CountDownLatch(1);
            countDownLatch.await(60, TimeUnit.SECONDS);
        }
    }

    static Supplier<ProducerRecord<String, BeerPurchase>> beerPurchaseProducerRecordSupplier = new Supplier<>() {
        private final Faker faker = new Faker();
        private final Beer beerFaker = faker.beer();
        private final Number numberFaker = faker.number();
        private final BeerPurchase.Builder builder = BeerPurchase.newBuilder();
        @Override
        public ProducerRecord<String, BeerPurchase> get() {
            BeerPurchase purchase = builder.setBeerType(beerFaker.name())
                    .setTotalSale(numberFaker.randomDouble(2,10, 1000))
                    .setNumberCases(numberFaker.numberBetween(1, 50))
                    .setCurrency(BeerPurchase.Currency.forNumber(numberFaker.numberBetween(0,3)))
                    .build();
            return new ProducerRecord<>(INPUT_TOPIC, null, purchase);
        }
    };


}
