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
                        // FIX: Get the String, then convert it to a float[]
                        String vectorString = aiClient.getEmbedding(chunk); 
                        float[] vector = parseVector(vectorString);
                        
                        if (vector != null && vector.length > 0) {
                            db.insert(new Document(chunk, vector));
                            success = true;
                        }
                    } catch (Exception e) {
                        System.out.println("\nAPI Rate Limit hit. Pausing for " + (waitTime/1000) + " seconds...");
                        Thread.sleep(waitTime);
                        waitTime *= 2; 
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
            
            String answer = aiClient.generateAnswer(prompt); 
            
            System.out.println("AI: " + answer);
            
        } catch (Exception e) {
            System.out.println("Error generating answer: " + e.getMessage());
        }
    }

    // --- NEW HELPER METHOD ---
    // Converts a JSON array string like "[0.1, 0.2, 0.3]" into a Java float[] array
    private static float[] parseVector(String jsonArray) {
        if (jsonArray == null || jsonArray.trim().isEmpty()) {
            return new float[0];
        }
        
        // Remove the [ and ] brackets from the string
        String cleanString = jsonArray.replaceAll("\\[|\\]", "").trim();
        
        // Split the string by commas
        String[] parts = cleanString.split(",");
        float[] vector = new float[parts.length];
        
        // Convert each piece of text into a float number
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        
        return vector;
    }
}