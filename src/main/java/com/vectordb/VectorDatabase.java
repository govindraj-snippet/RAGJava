package com.vectordb;

import java.util.List;

public class VectorDatabase {

    private List<Document>documents ;

    public VectorDatabase(List<Document>documents ){
        this.documents = documents ; 
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
