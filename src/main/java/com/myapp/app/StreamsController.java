package com.kafka.app;

import org.apache.kafka.streams.KafkaStreams;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import static spark.Spark.*;

public class StreamsController {

    KafkaStreams streams;
    RpcClient rpcClient;

    public StreamsController(KafkaStreams streams) {
        this.streams = streams;
        this.rpcClient = new RpcClient(streams);
    }

    public void startStreamsController() {

        path("/api/state/projection", () -> {
            before("/*", (q, a) -> System.out.println("Received api call"));

            // get("/recordCount", (request, response) -> {
            //     String stateStore = "STATE_TABLE";
            //     int res = rpcClient.getAllStateStoresAsInt(stateStore);
            //     JSONObject result = new JSONObject();
            //     result.put("recordCount", res);
            //     response.type("application/json");
            //     return result;
            // });

            get("/allrecords", (request, response) -> {
                String stateStore = "AllRecords";
                JSONArray res = rpcClient.getAllGenericRecordStateStoresAsJson(stateStore);
                JSONObject result = new JSONObject();
                result.put("recordCount", res);
                response.type("application/json");
                return result;
            });

        });

    }
}
