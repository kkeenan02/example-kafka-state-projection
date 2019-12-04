package com.kafka.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.common.utils.Bytes;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.schemaregistry.avro.AvroCompatibilityLevel;

import myapp.schema.*;

public class StreamsTopology {

        Properties props;

        StreamsTopology(Properties props) {
                this.props = props;
        }

        public KafkaStreams getSteams() {

                StreamsBuilder builder = new StreamsBuilder();

                // initialize the genericAvro Serde
                Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", this.props.getProperty("schema.registry.url"));
                // serdeConfig.put("compatibilityLevel", "NONE");
                Serde<GenericRecord> genericAvroSerde = new GenericAvroSerde();
                
                final Serde<StateKey> StateKeyAvroSerde = new SpecificAvroSerde<>();
                StateKeyAvroSerde.configure(serdeConfig, true);
                
                final Serde<MultiRecord> MultiRecordAvroSerde = new SpecificAvroSerde<>();
                MultiRecordAvroSerde.configure(serdeConfig, false);
                
                final Serde<String> stringSerde = Serdes.String();
                genericAvroSerde.configure(serdeConfig, false); // `false` for record values

                // ** K-STREAMS ** //

                // Source node
                KStream<String, MultiRecord> RecordStream = builder.stream("RECORD_TOPIC",
                                Consumed.with(Serdes.String(), genericAvroSerde))
                                .mapValues(v -> {
                                        MultiRecord record = new MultiRecord();
                                        record.put("uuid", (v.get("uuid") == null) ? null : String.valueOf(v.get("uuid")));
                                        record.put("timestamp", (v.get("timestamp") == null) ? null : Long.valueOf(String.valueOf(v.get("timestamp"))));
                                        record.put("offset", (v.get("offset") == null) ? null : Integer.valueOf(String.valueOf(v.get("offset"))));
                                        record.put("data", (v.get("data") == null) ? null : String.valueOf(v.get("data")));
                                        record.put("applicationName", (v.get("applicationName") == null) ? null : String.valueOf(v.get("applicationName")));
                                        record.put("gitBranch", (v.get("gitBranch") == null) ? null : String.valueOf(v.get("gitBranch")));
                                        record.put("gitHash", (v.get("gitHash") == null) ? null : String.valueOf(v.get("gitHash")));
                                        record.put("Id", (v.get("Id") == null) ? null : String.valueOf(v.get("Id")));
                                        GenericData.Array<SingleRecord> RecordList = (GenericData.Array<SingleRecord>) v.get("SingleRecord");
                                        record.put("records", RecordList);
                                        return record;
                                });

                // Re-Key and Materialize records table
                KTable<StateKey, MultiRecord> StateTable = RecordStream
                                .map(new KeyValueMapper<String, MultiRecord, KeyValue<StateKey, MultiRecord>>() {

                                        @Override
                                        public KeyValue<StateKey, MultiRecord> apply(String k, MultiRecord v) {
                                                StateKey newKey = new StateKey();
                                                newKey.put("uuid", v.getUuid());
                                                newKey.put("offset", v.getOffset());
                                                return new KeyValue<>(newKey, v);
                                        }
                                })
                                .groupByKey(Grouped.with(StateKeyAvroSerde, MultiRecordAvroSerde))
                                .reduce((aggValue, newValue) -> {
                                        return newValue;
                                }, Materialized.<StateKey, MultiRecord, KeyValueStore<Bytes, byte[]>>as("AllRecords").withKeySerde(StateKeyAvroSerde).withValueSerde(MultiRecordAvroSerde));

                return new KafkaStreams(builder.build(), props);
        }
}
