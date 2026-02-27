package com.vectordb.cli; 

import com.vectordb.database.VectorDatabase;
import com.vectordb.models.Document;
import com.vectordb.parsers.DocumentParser;
import com.vectordb.parsers.ParserFactory;
import com.vectordb.parsers.TextChunker; 
import com.vectordb.api.NetworkClient;

import java.util.List;
import java.util.Scanner;

public class CLIApp {
    
    private static VectorDatabase db = new VectorDatabase();
    private static NetworkClient aiClient = new NetworkClient();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("==================================================");
        System.out.println("Enterprise RAG CLI Initialized!");
        System.out.println("Commands:");
        System.out.println("  /upload <filepath>  - Extract and chunk text");
        System.out.println("  /chat <question>    - Ask your documents a question");
        System.out.println("  /exit               - Close the application");
        System.out.println("==================================================");

        while (true) {
            System.out.print("\nUser > ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("/exit")) {
                System.out.println("Goodbye! Shutting down...");
                break;
            } 
            else if (input.startsWith("/upload ")) {
                String filePath = input.substring(8).trim();
                processUpload(filePath);
            } 
            else if (input.startsWith("/chat ")) {
                String question = input.substring(6).trim();
                processChat(question);
            }
            else {
                System.out.println("Unknown command. Use /upload or /chat");
            }
        }
        scanner.close();
    }

    private static void processUpload(String filePath) {
        try {
            DocumentParser parser = ParserFactory.getParser(filePath);
            System.out.println("Extracting text...");
            String extractedText = parser.extractText(filePath);
            
            System.out.println("Slicing text into smart chunks...");
            List<String> chunks = TextChunker.chunkText(extractedText, 100, 20);
            System.out.println("Created " + chunks.size() + " manageable chunks.");

            System.out.println("Memorizing document (Rate Limiter Active)...");
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                
                boolean success = false;
                int waitTime = 2000; 
                
               while (!success) {
                    try {
                        String vectorString = aiClient.getEmbedding(chunk); 
                        float[] vector = parseVector(vectorString);
                        
                        if (vector != null && vector.length > 0) {
                            db.insert(new Document(chunk, vector));
                            success = true;
                        } else {
                            System.out.println("\nWarning: API returned an empty response. Skipping chunk.");
                            break; 
                        }
                    } catch (Exception e) {
                      
                        System.out.println("\nAPI Error: " + e.getMessage()); 
                        System.out.println("Pausing for " + (waitTime/1000) + " seconds...");
                        
                        Thread.sleep(waitTime);
                        waitTime *= 2; 
                        
                      
                        if (waitTime > 120000) {
                            System.out.println("\nGiving up on this chunk. Moving to the next one.");
                            break;
                        }
                    }
                }
                System.out.print("\rSaved chunk " + (i + 1) + " of " + chunks.size());
            }
            System.out.println("\nDocument successfully memorized! You can now use /chat.");
            
        } catch (Exception e) {
            System.out.println("Error processing file: " + e.getMessage());
        }
    }

    private static void processChat(String question) {
        if (db.getSize() == 0) {
            System.out.println("The database is empty! Please /upload a document first.");
            return;
        }

        System.out.println("Thinking...");
        try {
            // FIX: Get the String, then convert it to a float[]
            String vectorString = aiClient.getEmbedding(question);
            float[] queryVector = parseVector(vectorString);
            
            Document bestMatch = db.search(queryVector);
            
            String prompt = "Answer the question using ONLY the provided context. " +
                            "Context: " + bestMatch.getText() + " " +
                            "Question: " + question;
            
           String rawResponse = aiClient.generateAnswer(prompt); 
            String cleanAnswer = parseAnswer(rawResponse);
            
            System.out.println("\nAI: " + cleanAnswer);
            
        } catch (Exception e) {
            System.out.println("Error generating answer: " + e.getMessage());
        }
    }

  // --- UPDATED HELPER METHOD ---
    private static float[] parseVector(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return new float[0];
        }
        
        try {
            // 1. Find where the actual array of numbers starts and ends
            int startIndex = jsonResponse.indexOf('[');
            int endIndex = jsonResponse.lastIndexOf(']');
            
            if (startIndex == -1 || endIndex == -1) {
                return new float[0]; // No array found
            }
            
            // 2. Extract just the text inside the brackets
            String arrayString = jsonResponse.substring(startIndex + 1, endIndex);
            
            // 3. Split the numbers by the comma
            String[] parts = arrayString.split(",");
            float[] vector = new float[parts.length];
            
            // 4. Convert each piece into a mathematical float
            for (int i = 0; i < parts.length; i++) {
                // Clean up any spaces or accidental quotes before parsing
                String cleanNum = parts[i].replaceAll("[\"\\s\\n\\r]", "");
                if (!cleanNum.isEmpty()) {
                    vector[i] = Float.parseFloat(cleanNum);
                }
            }
            
            return vector;
            
        } catch (Exception e) {
            System.out.println("Failed to parse vector math: " + e.getMessage());
            return new float[0];
        }
    }
    private static String parseAnswer( String jsonResponse){
        try{
            String searchString = "\"text\": \"" ; 
            int startIndex = jsonResponse.indexOf(searchString) ; 

            if(startIndex == -1 ) return jsonResponse ; 

            startIndex += searchString.length() ; 

            int endIndex = jsonResponse.indexOf("\"}]," , startIndex) ; 
            if(endIndex == -1 ) return jsonResponse ;

            String answer = jsonResponse.substring(startIndex, endIndex) ; 
           return answer.replace("\\n", "\n").replace("\\\"", "\"");
        } catch( Exception e ){
            return jsonResponse ; 
        }
    }
}