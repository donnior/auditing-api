package com.example.demo.llm;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.example.demo.util.Strings;

import lombok.Data;

@Data
public class SseEventData {
    private SseEventType event;
    private String text;
    private SseEventUsage usage;

    public SseEventData(SseEventType event, String text, SseEventUsage usage) {
        this.event = event;
        this.text = text;
        this.usage = usage;
    }

    public SseEventData(SseEventType event, String text) {
        this(event, text, null);
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            var data = Map.of("event", this.event.getValue(), "text", Strings.nullToEmpty(this.text));
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
