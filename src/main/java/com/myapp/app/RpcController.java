package com.kafka.app;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static spark.Spark.*;

import java.util.ArrayList;

// * Get a key-value pair from a KeyValue Store
// @Path("/keyvalue/{storeName}/{key}")

// * Get all of the key-value pairs available in a store
// @Path("/keyvalues/{storeName}/all")

// * Get all of the key-value pairs that have keys within the range from...to
// @Path("/keyvalues/{storeName}/range/{from}/{to}")

// * Query a window store for key-value pairs representing the value for a
// provided key within a range of window
// @Path("/windowed/{storeName}/{key}/{from}/{to}")

// * Get the metadata for all of the instances of this Kafka Streams application
// @Path("/instances")

// * Get the metadata for all instances of this Kafka Streams application that
// currently has the provided store.
// @Path("/instances/{storeName}")

// * Find the metadata for the instance of this Kafka Streams Application that
// has the given store and would have the given key if it exists.
// @Path("/instance/{storeName}/{key}")

public class RpcController {

    KafkaStreams streams;
    Rpc rpc;

    public RpcController(KafkaStreams streams) {
        this.streams = streams;
        this.rpc = new Rpc();
    }

    public void startRpcController() {

        // NOTE: here we are explicitly deserializing the statestore so that is will
        // happen on each instance to spread the load.
        // - we also convert to json to further spread the load an prevent a spike of
        // memory on a specific instance.

        get("/instances/stateStore/all/:storeName", (request, response) -> {
            String storeName = request.params(":storeName").toString();
            JSONArray result = this.rpc.stateStoreAll(this.streams, storeName, "GenericRecord");
            // specify the response type as json and return
            response.type("application/json");
            return result;
        });

        // get("/instances/stateStore/count/:storeName", (request, response) -> {
        //     String storeName = request.params(":storeName").toString();
        //     long result = this.rpc.stateStoreCount(this.streams, storeName);
        //     // return type should be long
        //     return result;
        // });

        // get("/instances/stateStore/all/key/:storeName/:key", (request, response) -> {
        //     String storeName = request.params(":storeName").toString();
        //     String key = request.params(":key").toString();
        //     JSONArray result = this.rpc.stateStoreByKey(this.streams, storeName, key);
        //     response.type("application/json");
        //     return result;
        // });

        // get("/instances/stateStore/all/long/:storeName", (request, response) -> {
        //     String storeName = request.params(":storeName").toString();
        //     JSONArray result = this.rpc.stateStoreAll(this.streams, storeName, "Long");
        //     // specify the response type as json and return
        //     response.type("application/json");
        //     return result;
        // });
    }
}
