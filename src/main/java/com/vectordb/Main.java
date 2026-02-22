
package com.vectordb;

import com.vectordb.NetworkClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class Main {
    public static void main(String args[]) {
        System.out.println("starting the application ");

        NetworkClient client = new NetworkClient();

        String textToEmbed = "Hello World ";
        System.out.println("Getting embedding for text: " + textToEmbed);

        String rawResponse = client.getEmbedding(textToEmbed);
        // float[] embedding ;

        if (rawResponse != null && !rawResponse.startsWith("Error")) {

            JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonObject("embedding").getAsJsonArray("values");

            float[] vector = new float[jsonArray.size()];

            for (int i = 0; i < jsonArray.size(); i++) {
                vector[i] = jsonArray.get(i).getAsFloat();
            }

            System.out.println("✅ Success! Extracted " + vector.length + " dimensions.");
            System.out.println("First 5 values: [" + vector[0] + ", " + vector[1] + ", " +
                    vector[2] + ", " + vector[3] + ", " + vector[4] + ", ...]");

            System.out.println("working perfectly !! ");

        } else {
            System.out.println("Failed to get embedding! ");
        }

    }

}
