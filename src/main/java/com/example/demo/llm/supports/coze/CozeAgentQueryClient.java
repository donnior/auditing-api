package com.example.demo.llm.supports.coze;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.util.Maps;
import com.example.demo.util.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.llm.AgentResponse;
import com.example.demo.llm.LlmRequestable;
import com.example.demo.llm.LlmStreamChunkConverter;
import com.example.demo.llm.SseChunk;
import com.example.demo.llm.SseEventType;
import com.example.demo.llm.SseFrame;
import com.example.demo.llm.AgentQueryClient;
import com.example.demo.util.Jsons;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class CozeAgentQueryClient implements AgentQueryClient {

    private static final Logger logger = LoggerFactory.getLogger(CozeAgentQueryClient.class);

    @Autowired
    private CozeConfigProperties cozeConfig;

    @Autowired
    private WebClient.Builder webClientBuilder;

    private LlmStreamChunkConverter chunkConverter = new CozeAgentChunkConverter();

    public CozeAgentQueryClient() {}

    public CompletableFuture<AgentResponse> chat(String botId, String text,
            Map<String, Object> context) {
        System.out.println("Coze Chat: " + botId + " : " + text);
        CompletableFuture<AgentResponse> future = new CompletableFuture<>();

        StringBuilder sb = new StringBuilder();

        var webClient = this.buildWebClient();
        var data = this.buildRequestData(botId, text, context, true);
        webClient
            .post()
            .uri("/v3/chat")
            .headers( h -> {
                h.setBearerAuth(this.cozeConfig.getApikey());
            })
            .body(Mono.just(data), Map.class)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            // .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
            .bodyToFlux(String.class)
            .map(this::parseSse)
            .doOnNext(event -> {
                if (event.data() != null) {
                    try {
                        Map<String, Object> eventData = Jsons.safeRead(Map.class, event.data());
                        Integer code = (Integer) Jsons.safeRead(eventData, "$.code");
                        if (code != null && code != 0) {
                            String message = (String) Jsons.safeRead(eventData, "$.msg");
                            logger.error("Coze streaming chat failed. code: {}, message: {}", code, message);
                            throw new RuntimeException("Coze API error: " + message);
                        }
                    } catch (Exception e) {
                        // 如果解析失败，可能不是错误响应，继续处理
                        System.out.println("parseSse error: " + e.getMessage());
                    }
                }
            })
            .map(this.chunkConverter::convert)
            .filter(SseChunk::notEmpty)
            .filter(SseChunk::notUnknown)
            .doOnError(error -> System.err.println("Error occurred: " + error.getMessage()))
            .doOnNext(event -> {
                if (event.data() != null && SseEventType.Answering.equals(event.event())) {
                    System.out.println("append answer: " + event.data().getText());
                    sb.append(Strings.nullToEmpty(event.data().getText()));
                }
            })
            .doOnComplete(() -> {
                future.complete(new AgentResponse(sb.toString()));
            })
            .subscribe(event -> {
                System.out.println(event);
            });

        return future;
    }


    private Map<String, Object> buildRequestData(String botId, String text, Map<String,Object> context, boolean isStream) {
        var common = Map.of(
            "bot_id", botId,
            "additional_messages", List.of(Map.of("role", "user", "content", text))
        );
        return isStream ? Maps.merge(common, Map.of("stream", true)) : common;
    }

    private WebClient buildWebClient() {
        return this.webClientBuilder
            .baseUrl(this.cozeConfig.getApibase())
            .defaultHeaders(h -> {
                h.setContentType(MediaType.APPLICATION_JSON);
                h.setBearerAuth(this.cozeConfig.getApikey());
            })
			.build();
    }

    public SseFrame parseSse(String raw) {
        String event = null;
        String id = null;
        Integer retry = null;
        StringBuilder data = new StringBuilder();

        for (String line : raw.split("\n")) {
            if (line.isBlank())
                continue;

            int idx = line.indexOf(':');
            if (idx < 0)
                continue;

            String field = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();

            switch (field) {
                case "event" -> event = value;
                case "id" -> id = value;
                case "retry" -> retry = Integer.valueOf(value);
                case "data" -> {
                    if (!data.isEmpty())
                        data.append('\n');
                    data.append(value);
                }
            }
        }

        return new SseFrame(event, id, retry, data.toString());
    }
}
