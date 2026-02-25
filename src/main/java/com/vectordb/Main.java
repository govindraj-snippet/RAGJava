package com.vectordb;

import com.vectordb.NetworkClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.vectordb.Document;
import com.vectordb.VectorDatabase;

public class Main {

    public static VectorDatabase db = new VectorDatabase(); 

    public static void main(String args[]) {
        System.out.println("Starting the Vector Database...");

        NetworkClient client = new NetworkClient();

       String[] facts = {
            "The Apollo 11 mission launched on July 16, 1969. Astronauts Neil Armstrong and Buzz Aldrin became the first humans to walk on the moon, while Michael Collins piloted the command module in orbit.",
            "The Perseverance rover safely landed on Mars inside the Jezero Crater on February 18, 2021. Its primary mission is to seek signs of ancient microscopic life and collect rock samples. It also carried a small experimental drone helicopter named Ingenuity.",
            "Launched in December 2021, the James Webb Space Telescope (JWST) is the largest optical telescope in space. It is designed to observe some of the most distant and ancient galaxies in the universe using high-resolution infrared instruments.",
            "Voyager 1 is a space probe launched by NASA in 1977. It officially crossed the heliopause to enter interstellar space in 2012. It carries a time capsule known as the Golden Record, which contains sounds and images portraying the diversity of life on Earth to any extraterrestrial intelligence that might find it."
        };

        System.out.println("Saving documents to memory...");
        
        for (String fact : facts) {
            float[] vector = getVectorFromAPI(client, fact); 
            
            if (vector != null) {
                Document doc = new Document(fact, vector);
                db.insert(doc);
                System.out.println("Saved: '" + fact + "'");
            }
        }

        System.out.println("Database populated! Total documents: " + db.getSize());

        String searchQuery = "What is the name of the helicopter on Mars, and what crater did the rover land in?";
        System.out.println("Searching Database for: '" + searchQuery + "'\n");

        float[] queryVector = getVectorFromAPI(client, searchQuery);

        if (queryVector != null) {
        
            Document bestMatch = db.search(queryVector);
            
            if (bestMatch != null) {
                System.out.println("Found Context: " + bestMatch.getText()); 
                System.out.println("\nSending Context and Question to the AI..."); 

                // FIX 1: Removed \n characters. We use spaces instead so the JSON doesn't break!
                String prompt = "You are a helpful assistant. Answer the user's question using ONLY the provided context. " +
                                "Context: " + bestMatch.getText() + " " +
                                "Question: " + searchQuery;
                
                // FIX 2: Spelling matched to standard 'generateAnswer'
                String rawResponse = client.generateAnswer(prompt);
                String finalAnswer = parseChatResponse(rawResponse); 

                System.out.println("AI's Answer: " + finalAnswer);

            } else {
                System.out.println("No match found.");
            }
        } else {
            System.out.println("Failed to get query embedding!");
        }
    }

    public static float[] getVectorFromAPI(NetworkClient client, String textToEmbed) {
        String rawResponse = client.getEmbedding(textToEmbed);
        
        if (rawResponse != null && !rawResponse.startsWith("Error")) {
            JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonObject("embedding").getAsJsonArray("values");

            float[] vector = new float[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                vector[i] = jsonArray.get(i).getAsFloat();
            }
            return vector;
        }
        return null; 
    }

    public static String parseChatResponse(String json){
        // FIX 3: Stop immediately if the network returned an error string!
        if (json == null || json.startsWith("Error")) {
            return json; 
        }

        try {
            JsonObject jsonObject  = JsonParser.parseString(json).getAsJsonObject(); 
            String answer = jsonObject.getAsJsonArray("candidates").get(0)
                            .getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts")
                            .get(0).getAsJsonObject().get("text").getAsString();
                            
            return answer.trim();      
        } catch( Exception e ){
            return "error: " + e.getMessage(); 
        }
    }
}