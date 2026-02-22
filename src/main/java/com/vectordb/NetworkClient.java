package com.vectordb;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import io.github.cdimascio.dotenv.Dotenv;

public class NetworkClient {

    // private static final Dotenv dotenv = Dotenv.configure()
    //         .directory("E:\\GOPROGRAM\\java projects\\VectorDB-Lite")
    //         .load();
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";
    private static final String API_KEY ="AIzaSyCOQMoMGhbIZAs1sFT93Uktewrm0A-b19k" ; 

    public String getEmbedding( String text ){

String jsonPayload = "{\n" +
        "  \"model\": \"models/gemini-embedding-001\",\n" + 
        "  \"content\": {\n" +
        "    \"parts\": [{\n" +
        "      \"text\": \"" + text + "\"\n" +
        "    }]\n" +
        "  }\n" +
        "}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key" , API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        
        try{
            HttpResponse<String>response = client.send( request , HttpResponse.BodyHandlers.ofString()) ; 

            if(response.statusCode() == 200 ){
                return  response.body() ;
            }
            else{
                return "Error:" + response.statusCode() + "\n" + response.body() ; 
            }

        } catch( Exception e ){
            System.out.println("Network request failed! ") ; 
            e.printStackTrace() ;
            return null ; 
        }

    }

    
}
