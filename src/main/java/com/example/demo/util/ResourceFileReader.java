package com.example.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.data.repository.init.ResourceReader;
import org.springframework.util.FileCopyUtils;
public class ResourceFileReader {

    public static String readResourceFile(String filePath) throws IOException {
        ClassLoader classLoader = ResourceReader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(filePath);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) { // 指定字符编码

            if (inputStream == null) {
                throw new IOException("Resource not found: " + filePath);
            }

            return FileCopyUtils.copyToString(reader);
        }
    }

}
