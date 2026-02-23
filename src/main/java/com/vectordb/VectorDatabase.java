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

    



    
}
