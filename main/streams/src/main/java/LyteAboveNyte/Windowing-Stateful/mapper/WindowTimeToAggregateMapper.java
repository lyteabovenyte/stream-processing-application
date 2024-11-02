package bbejeck.Processor-API.mapper;

import bbejeck.Processor-API.IotSensorAggregation;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Windowed;

public class WindowTimeToAggregateMapper implements KeyValueMapper<Windowed<String>,IotSensorAggregation, KeyValue<String, IotSensorAggregation>> {
    @Override
    public KeyValue<String, IotSensorAggregation> apply(Windowed<String> windowed,
                                                        IotSensorAggregation iotSensorAggregation) {
        long start = windowed.window().start();
        long end = windowed.window().end();
        iotSensorAggregation.setWindowStart(start);
        iotSensorAggregation.setWindowEnd(end);
        return KeyValue.pair(windowed.key(), iotSensorAggregation);
    }
}


