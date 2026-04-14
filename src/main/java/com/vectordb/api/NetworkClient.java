package com.vectordb.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {

    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";
    private static final String API_KEY; 

    static {
        // Load the API_KEY from the src/.env file
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure().directory("src").load();
        API_KEY = dotenv.get("API_KEY");
    }

    public String getEmbedding(String text){

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
            HttpResponse<String> response = client.send(request , HttpResponse.BodyHandlers.ofString()); 

            if(response.statusCode() == 200){
                return response.body();
            } else {
                return "Error:" + response.statusCode() + "\n" + response.body(); 
            }

        } catch(Exception e){
            System.out.println("Network request failed! "); 
            e.printStackTrace();
            return null; 
        }
    }

    public String generateAnswer(String prompt){
        try{
           String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true); 

            String cleanPrompt = prompt.replace("\\", "\\\\")
                                       .replace("\"", "\\\"")
                                       .replace("\n", " ")
                                       .replace("\r", " ");

            // The Upgraded Payload:
String jsonInputString = "{" +
    "\"contents\": [{\"parts\": [{\"text\": \"" + cleanPrompt + "\"}]}]," +
    "\"generationConfig\": {" +
        "\"maxOutputTokens\": 1000," +
        "\"temperature\": 0.3" + // 0.3 keeps the AI focused and factual
    "}" +
"}";
            
            try(OutputStream os = conn.getOutputStream()){
                byte[] input = jsonInputString.getBytes("utf-8"); 
                os.write(input , 0 , input.length);
            }

          
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
              
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
                return "Error from Google: " + errorResponse.toString();
            }

         
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream() , "utf-8"));
            StringBuilder response = new StringBuilder(); 
            String responseLine; 
            while((responseLine = br.readLine()) != null){
                response.append(responseLine.trim()); 
            }
            return response.toString(); 

        } catch(Exception e){
            return "Error: " + e.getMessage();
        }
    }
}