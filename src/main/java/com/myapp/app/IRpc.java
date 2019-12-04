package com.kafka.app;

import org.apache.kafka.streams.KafkaStreams;
import org.json.simple.JSONArray;

interface IRpc {

    public JSONArray stateStoreAll(KafkaStreams streams, String storeName, String valueDeserializerType);

}