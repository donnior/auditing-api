package com.example.demo.llm;

import org.springframework.http.codec.ServerSentEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class ServerSentEvents {

    private ObjectMapper objectMapper;

    public ServerSentEvents() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ServerSentEvent<Object> from(SseChunk chunk) {
        try {
            return ServerSentEvent.builder().event(chunk.event().getValue())
                .data(objectMapper.writeValueAsString(chunk.data()))
                .build();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
