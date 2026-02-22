
package com.vectordb;

import com.vectordb.NetworkClient;

public class Main {
    public static void main( String args[]){
        System.out.println("starting the application ") ; 

        NetworkClient client = new NetworkClient(); 

        String textToEmbed = "Hello World " ; 
        System.out.println("Getting embedding for text: " + textToEmbed) ;

        String rawResponse = client.getEmbedding(textToEmbed ) ; 

        if(rawResponse != null ){
            System.out.println("response : " ) ; 
            System.out.println(rawResponse) ;

        }
        else{
            System.out.println("Failed to get embedding! ") ; 
        }


    }

}
