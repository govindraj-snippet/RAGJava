package com.vectordb.parsers;

import java.util.ArrayList ; 
import java.util.List ; 


public class TextChunker {

    public static List<String>chunkText(String text , int maxWords , int overlap){

        List<String>chunks = new ArrayList<>() ; 

        if(text == null || text.trim().isEmpty()){
            return chunks ; 
        }

        String[] words = text.split("\\s+") ; 

        int i = 0 ; 
        while( i < words.length){
            StringBuilder chunk = new StringBuilder() ; 

            int end = Math.min( i + maxWords , words.length ) ; 

            for(int j = i ; j < end ; j++ ){
                chunk.append(words[j]).append(" " ) ; 
            }
            chunks.add(chunk.toString().trim()) ; 

            i+= (maxWords - overlap) ; 

        }
        return chunks ; 

    }
    
}
