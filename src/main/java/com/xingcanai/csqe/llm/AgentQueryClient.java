package com.xingcanai.csqe.llm;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.http.codec.ServerSentEvent;


import reactor.core.publisher.Flux;

public interface AgentQueryClient {

    // public Flux<ServerSentEvent<String>> streamingChat(String agentId, List<? extends LlmRequestable> messages, Map<String,Object> context);

    public CompletableFuture<AgentResponse> chat(String agentId, String text, Map<String,Object> context);

    public default String chatSync(String agentId, String text, Map<String, Object> context) {
        CompletableFuture<AgentResponse> future = this.chat(agentId, text, context);

        try {
            AgentResponse assistantMessage = future.get();
            return assistantMessage != null ? assistantMessage.getText() : null;
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}
