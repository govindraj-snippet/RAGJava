
package com.vectordb;

import com.vectordb.NetworkClient;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.vectordb.Document;
import com.vectordb.VectorDatabase;



public class Main {

   public static VectorDatabase db = new VectorDatabase() ; 

    public static void main(String args[]) {
        System.out.println("starting the application ");

        NetworkClient client = new NetworkClient();

        String [] facts = {
            "I love eating pepperoni pizza." , 
            "The stock market crashed today." , 
            "My favorite animal is golden retriever."
        };

        System.out.println("Saving documents to memory...") ; 

        for(String fact : facts ){
            
            float [] vector = getVectorFromAPI(client, fact) ; 
            if( vector != null ){
                Document doc = new Document(fact , vector) ; 
                db.insert(doc) ; 
                System.out.println("Saved doc:" + fact + "'") ; 
            }
        }

        String textToEmbed = "Hello World ";
        System.out.println("Getting embedding for text: " + textToEmbed);

        String rawResponse = client.getEmbedding(textToEmbed);
        
        if (rawResponse != null && !rawResponse.startsWith("Error")) {

            JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonObject("embedding").getAsJsonArray("values");

            float[] vector = new float[jsonArray.size()];
          
            for (int i = 0; i < jsonArray.size(); i++) {
                vector[i] = jsonArray.get(i).getAsFloat();
            }

            Document doc = new Document(textToEmbed, vector) ;

            db.insert(doc) ;
            
            System.out.println("doc saved succesfull " + db.getSize()) ; 

          

        } else {
            System.out.println("Failed to get embedding! ");
        }

    }

    public static float[] getVectorFromAPI(NetworkClient client , String textToEmbed){
        String rawResponse = client.getEmbedding(textToEmbed) ; 

        if( rawResponse != null && !rawResponse.startsWith("Error")){

            JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject() ; 
            JsonArray jsonArray = jsonObject.getAsJsonObject("embedding").getAsJsonArray("values");

            float [] vector = new float[jsonArray.size()] ; 
            for(int i = 0 ; i < jsonArray.size() ; i++ ){
                vector[i] = jsonArray.get(i).getAsFloat() ; 
            }
            return vector ; 
        }
        return null ; 

    }

}
