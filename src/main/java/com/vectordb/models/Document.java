package com.vectordb.models;


public class Document {

    private String text ; 
    private float[] embedding ; 

    public Document( String text , float[] embedding){
        this.text = text ; 
        this.embedding = embedding ; 
    }

    public String getText(){
        return this.text ; 
    }
    
    public float[] getEmbedding(){
        return this.embedding ; 
    }

    
}
