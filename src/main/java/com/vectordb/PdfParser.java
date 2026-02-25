package com.vectordb;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;



public class PdfParser implements DocumentParser {
    @Override
    public String extractText(String filePath) throws Exception {
        
       try(PDDocument document = PDDocument.load(new File(filePath))){

            PDFTextStripper pdfStripper = new PDFTextStripper() ; 
            return pdfStripper.getText(document) ; 
       }
    }
    
}
