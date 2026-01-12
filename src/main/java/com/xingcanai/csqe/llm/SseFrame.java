package com.xingcanai.csqe.llm;

public record SseFrame(
        String event,
        String id,
        Integer retry,
        String data
) {}
