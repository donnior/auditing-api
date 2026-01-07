package com.example.demo.llm;

import org.springframework.http.codec.ServerSentEvent;

public interface LlmStreamChunkConverter {

    // SseChunk convert(ServerSentEvent<String> message);

    SseChunk convert(SseFrame frame);

}
