package com.example.demo.llm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.codec.ServerSentEvent;

import reactor.core.publisher.Flux;

public interface ModelClient {

    public Flux<ServerSentEvent<String>> streamingChat(String model, String instruction, List<? extends LlmRequestable> messages, Map<String,Object> context);

    public CompletableFuture<AgentResponse> chat(String model, String instruction, List<? extends LlmRequestable> messages, Map<String,Object> context);

    public boolean isSupported(String model);
}
