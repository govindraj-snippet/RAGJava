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
            System.out.println("Extracting text...");
            String extractedText = "";
            String lowerPath = filePath.toLowerCase();

            // 1. Check if it's a PDF or a TXT
            if (lowerPath.endsWith(".pdf")) {
                DocumentParser parser = ParserFactory.getParser(filePath);
                extractedText = parser.extractText(filePath);
            } 
            else if (lowerPath.endsWith(".txt")) {
                // Read the TXT file directly using Java's built-in Files class
                extractedText = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
            } 
            else {
                System.out.println("Error: Unsupported file format. Please upload a .pdf or .txt file.");
                return;
            }

            System.out.println("Slicing text into smart chunks...");
            List<String> chunks = TextChunker.chunkText(extractedText, 500, 100);
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
            String safePrompt = prompt.replace("\\", "\\\\")  // Escape slashes
                                  .replace("\"", "\\\"")  // Escape quotes
                                  .replace("\n", "\\n")   // Escape newlines
                                  .replace("\r", "")      // Strip carriage returns
                                  .replace("\t", "\\t");  // Escape TABS (The Bitcoin Fix!)
            
        //    String rawResponse = aiClient.generateAnswer(prompt); 
        //     String cleanAnswer = parseAnswer(rawResponse);
            String rawResponse = aiClient.generateAnswer(safePrompt); 
            
            // --- NEW DEBUGGING LINES ---
            System.out.println("\n====== RAW API RESPONSE ======");
            System.out.println(rawResponse);
            System.out.println("==============================\n");
            
            String cleanAnswer = parseAnswer(rawResponse);
       //     System.out.println("AI: " + cleanAnswer);
            
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
  public static String parseAnswer(String jsonResponse) {
        try {
            String searchKey = "\"text\": \"";
            int startIndex = jsonResponse.indexOf(searchKey);
            
            // If the API sent an error and "text" isn't there, print the raw error!
            if (startIndex == -1) {
                return "\n[API ERROR OR SAFETY BLOCK]\nRaw Response from Google:\n" + jsonResponse;
            }
            
            startIndex += searchKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            
            if (endIndex == -1) {
                return "\n[JSON FORMAT ERROR]\nRaw Response:\n" + jsonResponse;
            }
            
            String answer = jsonResponse.substring(startIndex, endIndex);
            return answer.replace("\\n", "\n").replace("\\t", "\t").replace("\\\"", "\"");
            
        } catch (Exception e) {
            return "CRITICAL PARSE ERROR: " + e.getMessage();
        }
    }
}