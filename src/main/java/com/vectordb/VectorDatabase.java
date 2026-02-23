package com.vectordb;

import java.util.ArrayList;
import java.util.List;

public class VectorDatabase {

    private List<Document>documents ;

    public VectorDatabase( ){
        this.documents = new ArrayList<>() ; 
    }
    public void insert(Document doc ){
        this.documents.add(doc) ;
    }
    public List<Document> getDocuments(){
        return this.documents ; 
    }
    public int getSize(){
        return this.documents.size() ; 
    }

    public Document search (float [] queryVector){

        double bestScore = -1.0 ; 
        Document bestMatch = null ; 

        for( Document doc : this.documents ){

            double score = VectorMath.cosineSimilarity( queryVector , doc.getEmbedding()) ; 

            if( score > bestScore ){
                bestScore = score ; 
                bestMatch = doc ; 
            }
        }

        if( bestMatch != null ){
            System.out.println("Match Found! Similarity Score: " + bestScore ) ; 
        }

        return bestMatch ; 


    }

        

    



    
}
