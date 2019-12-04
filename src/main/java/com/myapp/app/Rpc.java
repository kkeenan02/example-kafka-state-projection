package com.kafka.app;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Rpc implements IRpc {

    // @Override
    // public long stateStoreCount(KafkaStreams streams, String storeName) {
    //     ReadOnlyKeyValueStore<String, GenericRecord> stateStore = streams.store(storeName,
    //             QueryableStoreTypes.<String, GenericRecord>keyValueStore());
    //     return stateStore.approximateNumEntries();
    // }

    @Override
    public JSONArray stateStoreAll(KafkaStreams streams, String storeName, String valueDeserializerType) {

        ReadOnlyKeyValueStore<?, ?> stateStore = null;
        stateStore = streams.store(storeName, QueryableStoreTypes.<GenericRecord, GenericRecord>keyValueStore());
        KeyValueIterator<?, ?> stateData = stateStore.all();

        JSONArray result = new JSONArray();
        JSONParser parser = new JSONParser();

        while (stateData.hasNext()) {
            KeyValue<?, ?> row = stateData.next();
            JSONObject json = new JSONObject();
            System.out.println(String.valueOf(row.value));
            try {
                if (valueDeserializerType == "GenericRecord") {
//                    (JSONObject) parser.parse(String.valueOf(row.key)),
                    json.put("row", (JSONObject) parser.parse(String.valueOf(row.value)));
                } else if (valueDeserializerType == "Long") {
                    json.put(row.key, row.value.toString());
                }
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            result.add(json);
        }
        return result;
    }

//    @Override
//    public JSONArray stateStoreAll(KafkaStreams streams, String storeName, String valueDeserializerType) {
//        ReadOnlyKeyValueStore<?, ?> stateStore = null;
//        if (valueDeserializerType == "GenericRecord") {
//            stateStore = streams.store(storeName, QueryableStoreTypes.<GenericRecord, GenericRecord>keyValueStore());
//        } else if (valueDeserializerType == "Long") {
//            stateStore = streams.store(storeName, QueryableStoreTypes.<String, Long>keyValueStore());
//        }
//        KeyValueIterator<?, ?> stateData = stateStore.all();
//
//        JSONArray result = new JSONArray();
//        JSONParser parser = new JSONParser();
//
//        while (stateData.hasNext()) {
//            KeyValue<?, ?> row = stateData.next();
//            JSONObject json = new JSONObject();
//            try {
//                if (valueDeserializerType == "GenericRecord") {
//                    json.put(row.key, (JSONObject) parser.parse(String.valueOf(row.value)));
//                } else if (valueDeserializerType == "Long") {
//                    json.put(row.key, row.value.toString());
//                }
//            } catch (ParseException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            result.add(json);
//        }
//        return result;
//    }

    // @Override
    // public JSONArray stateStoreByKey(KafkaStreams streams, String storeName, String key) {
    //     ReadOnlyKeyValueStore<String, GenericRecord> stateStore = streams.store(storeName,
    //             QueryableStoreTypes.<String, GenericRecord>keyValueStore());

    //     String keyValue = stateStore.get(key).toString();

    //     JSONParser parser = new JSONParser();
    //     JSONObject json = new JSONObject();
    //     try {
    //         json.put(key, (JSONObject) parser.parse(keyValue));
    //     } catch (ParseException e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //     }

    //     JSONArray result = new JSONArray();
    //     result.add(json);

    //     return result;
    // }
}