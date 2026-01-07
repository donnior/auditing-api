package com.example.demo.llm;

public record SseFrame(
        String event,
        String id,
        Integer retry,
        String data
) {}
