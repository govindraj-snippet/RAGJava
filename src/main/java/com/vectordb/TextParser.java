package com.vectordb;

import java.nio.file.Files;
import java.nio.file.Path;

public class TextParser implements DocumentParser {
    @Override
    public String extractText(String filePath) throws Exception {
        // Java's built-in way to quickly read an entire text file
        return Files.readString(Path.of(filePath));
    }
}