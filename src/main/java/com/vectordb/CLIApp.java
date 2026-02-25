package com.vectordb;

import java.util.Scanner;

public class CLIApp {
    public static void main( String [] args ){

        Scanner sc = new Scanner(System.in) ; 

        System.out.println("Welcome!!") ; 
        System.out.println("Commands:") ; 
        System.out.println("  /upload <filepath>  - Extract text from PDF, DOCX, or TXT");
        System.out.println("  /exit               - Close the application");

        while(true){
            System.out.println("\n User > ") ; 
            String input = sc.nextLine().trim() ; 

            if(input.equalsIgnoreCase("/exit")){
                System.out.println("Goodbye ! shutting down..") ; 
                break ; 
            }
            else if(input.startsWith("/upload")){
                String filePath = input.substring(8).trim() ; 
                processUpload(filePath) ; 

            } else {
                System.out.println("⚠️ Chat is not connected yet! Try uploading a file with /upload <filepath>");
            }
        }
        sc.close() ; 

    }

    private static void processUpload(String filePath){
        try{
            DocumentParser parser = ParserFactory.getParser(filePath) ; 
            System.out.println("Extracting text using" +parser.getClass().getSimpleName() + "...") ; 
            String extractedText = parser.extractText(filePath) ;

            System.out.println("Extracted Text:\n" + extractedText) ;


        } catch(IllegalArgumentException e ){
            System.out.println(e.getMessage()) ;
        }
        catch(Exception e){
            System.out.println("Error processing file: " + e.getMessage()) ; 
        }
    }
    
}
