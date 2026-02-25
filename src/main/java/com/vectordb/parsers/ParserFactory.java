package com.vectordb.parsers;

public class ParserFactory {
    
    public static DocumentParser getParser(String filePath) {
        String lowerPath = filePath.toLowerCase();
        
        if (lowerPath.endsWith(".pdf")) {
            // We will build this class next!
            return new PdfParser(); 
        } 
        else if (lowerPath.endsWith(".docx")) {
            // We will build this class next!
            return new WordParser(); 
        } 
        else if (lowerPath.endsWith(".txt")) {
            // We will build this class next!
            return new TextParser(); 
        } 
        else {
            throw new IllegalArgumentException("Unsupported file type: " + filePath);
        }
    }
}