package com.kafka.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
// import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarException;

// import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

class RpcClient {

    KafkaStreams streams;

    RpcClient(KafkaStreams streams) {
        this.streams = streams;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static Integer readIntFromUrl(String url) throws IOException, JarException, ParseException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return Integer.valueOf(readAll(rd));
        } finally {
            is.close();
        }
    }

    public static JSONArray readJsonFromUrl(String url)
            throws IOException, JarException, ParseException, org.json.simple.parser.ParseException {
        InputStream is = new URL(url).openStream();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONArray json = (JSONArray) parser.parse(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    // public int getAllStateStoresAsInt(String stateStore) {
    //     // get all metadata for stateStore
    //     Collection<StreamsMetadata> tableMetadata = this.streams.allMetadataForStore(stateStore);
    //     // iterate over meta data and call RPC end points of each instance
    //     ArrayList<Integer> intArray = new ArrayList<>();
    //     tableMetadata.forEach(meta -> {
    //         String url = "http://" + meta.host().toString() + ":" + String.valueOf(meta.port())
    //                 + "/instances/stateStore/count/" + stateStore;
    //         // agg result count
    //         try {
    //             intArray.add(readIntFromUrl(url));
    //         } catch (IOException | ParseException e) {
    //             // TODO Auto-generated catch block
    //             e.printStackTrace();
    //         }
    //     });
    //     int result = 0;
    //     for (int i = 0; i < intArray.size(); i++) {
    //         result += intArray.get(i);
    //     }
    //     // return JSONArray
    //     return result;
    // }

    public JSONArray getAllGenericRecordStateStoresAsJson(String stateStore) {
        // get all metadata for stateStore
        Collection<StreamsMetadata> tableMetadata = this.streams.allMetadataForStore(stateStore);
        // iterate over meta data and call RPC end points of each instance
        JSONArray result = new JSONArray();
        tableMetadata.forEach(meta -> {
            String url = "http://" + meta.host().toString() + ":" + String.valueOf(meta.port())
                    + "/instances/stateStore/all/" + stateStore;
            // read from url as JSON ARRAY
            JSONArray json;
            try {
                json = readJsonFromUrl(url);
                System.out.println("Calling: url > " + url);
                // append data to result JSONArray
                result.addAll(json);
            } catch (IOException | ParseException | org.json.simple.parser.ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        // return JSONArray
        return result;
    }

    // public JSONArray getKeyGenericRecordStateStoresAsJson(String stateStore, String key)
    //         throws JarException, IOException, ParseException, org.json.simple.parser.ParseException {
    //     // get all metadata for stateStore
    //     StreamsMetadata metadata = this.streams.metadataForKey(stateStore, key, Serdes.String().serializer());
    //     // iterate over meta data and call RPC end points of each instance
    //     JSONArray result = new JSONArray();

    //     if (metadata != null) {
    //         String url = "http://" + metadata.host().toString() + ":" + String.valueOf(metadata.port())
    //                 + "/instances/stateStore/all/key/" + stateStore + "/" + key;
    //         // read from url as JSON ARRAY
    //         JSONArray json = readJsonFromUrl(url);
    //         System.out.println("Calling: url > " + url);
    //         // append data to result JSONArray
    //         result.addAll(json);
    //     }
    //     // return JSONArray
    //     return result;
    // }

    // public JSONArray getAllLongStateStoresAsJson(String stateStore) {
    //     // get all metadata for stateStore
    //     Collection<StreamsMetadata> tableMetadata = this.streams.allMetadataForStore(stateStore);
    //     // iterate over meta data and call RPC end points of each instance
    //     JSONArray result = new JSONArray();
    //     tableMetadata.forEach(meta -> {
    //         String url = "http://" + meta.host().toString() + ":" + String.valueOf(meta.port())
    //                 + "/instances/stateStore/all/long/" + stateStore;
    //         // read from url as JSON ARRAY
    //         JSONArray json;
    //         try {
    //             json = readJsonFromUrl(url);
    //             System.out.println("Calling: url > " + url);
    //             // append data to result JSONArray
    //             result.addAll(json);
    //         } catch (IOException | ParseException | org.json.simple.parser.ParseException e) {
    //             // TODO Auto-generated catch block
    //             e.printStackTrace();
    //         }
    //     });
    //     // return JSONArray
    //     return result;
    // }

    // public JSONArray getAllStateStoresFilteredAsJson(String stateStore, String filterKey, String filterValue) {
    //     // get all metadata for stateStore
    //     Collection<StreamsMetadata> tableMetadata = this.streams.allMetadataForStore(stateStore);
    //     // iterate over meta data and call RPC end points of each instance
    //     JSONArray result = new JSONArray();
    //     tableMetadata.forEach(meta -> {
    //         String url = "http://" + meta.host().toString() + ":" + String.valueOf(meta.port())
    //                 + "/instances/stateStore/all/" + stateStore + "/" + filterKey + "/" + filterValue;
    //         // read from url as JSON ARRAY
    //         JSONArray json;
    //         try {
    //             json = readJsonFromUrl(url);
    //             System.out.println("Calling: url > " + url);
    //             // append data to result JSONArray
    //             result.addAll(json);
    //         } catch (IOException | ParseException | org.json.simple.parser.ParseException e) {
    //             // TODO Auto-generated catch block
    //             e.printStackTrace();
    //         }
    //     });
    //     // return JSONArray
    //     return result;
    // }
}