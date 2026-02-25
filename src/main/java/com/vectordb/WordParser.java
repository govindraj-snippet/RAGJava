package com.vectordb;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.FileInputStream;

public class WordParser implements DocumentParser {
    @Override 
    public String extractText( String filePath ) throws Exception{

        try(FileInputStream fis = new FileInputStream(filePath) ; 
            XWPFDocument document = new XWPFDocument(fis) ; 
            XWPFWordExtractor extractor = new XWPFWordExtractor(document)){

            return extractor.getText() ;
        }
    }
    
}
