package com.xingcanai.csqe.llm.supports.deepseek;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class DeepSeekClient {

    private static final int MAX_RESPONSE_BYTES = 16 * 1024 * 1024;
    private static final int MAX_ERROR_RESPONSE_CHARS = 4_096;

    private final DeepSeekProperties properties;
    private final WebClient webClient;

    public DeepSeekClient(WebClient.Builder webClientBuilder, DeepSeekProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(MAX_RESPONSE_BYTES))
                .build();
    }

    public DeepSeekResult complete(String systemPrompt, String userPrompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new DeepSeekException(
                    "CONFIGURATION",
                    false,
                    "DeepSeek API key is not configured");
        }

        Map<String, Object> request = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "thinking", Map.of("type", properties.isThinkingEnabled() ? "enabled" : "disabled"),
                "reasoning_effort", properties.getReasoningEffort(),
                "temperature", properties.getTemperature(),
                "max_tokens", properties.getMaxTokens(),
                "stream", false);

        Mono<ChatCompletionResponse> responseMono = webClient
                .post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .bodyValue(request)
                .exchangeToMono(response -> {
                    int status = response.statusCode().value();
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ChatCompletionResponse.class);
                    }

                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> Mono.error(new DeepSeekException(
                                    "HTTP_" + status,
                                    isRetryableStatus(status),
                                    failureMessage(status, body))));
                })
                .retryWhen(Retry.fixedDelay(properties.getMaxRetries(), properties.getRetryDelay())
                        .filter(this::isAutomaticallyRetryable)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .timeout(properties.getTimeout())
                .onErrorMap(
                        TimeoutException.class,
                        exception -> new DeepSeekException(
                                "TIMEOUT",
                                false,
                                "DeepSeek request timed out",
                                exception));

        ChatCompletionResponse response = responseMono.block();
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new DeepSeekException("EMPTY_RESPONSE", false, "DeepSeek response has no readable choice");
        }

        Choice choice = response.choices().getFirst();
        String content = choice.message() == null ? null : choice.message().content();
        if (content == null || content.isBlank()) {
            throw new DeepSeekException("EMPTY_RESPONSE", false, "DeepSeek response has no readable content");
        }

        Usage usage = response.usage();
        return new DeepSeekResult(
                content,
                response.model() == null ? properties.getModel() : response.model(),
                choice.finishReason(),
                usage == null ? null : usage.promptTokens(),
                usage == null ? null : usage.completionTokens(),
                usage == null ? null : usage.totalTokens());
    }

    private boolean isAutomaticallyRetryable(Throwable throwable) {
        return throwable instanceof DeepSeekException exception && exception.isAutomaticRetryAllowed();
    }

    private static boolean isRetryableStatus(int status) {
        return status == 429 || status == 500 || status == 503;
    }

    private static String failureMessage(int status, String responseBody) {
        String body = responseBody == null
                ? ""
                : responseBody.strip().replace('\r', ' ').replace('\n', ' ');
        if (body.isEmpty()) {
            return "DeepSeek request failed with HTTP " + status;
        }
        if (body.length() > MAX_ERROR_RESPONSE_CHARS) {
            body = body.substring(0, MAX_ERROR_RESPONSE_CHARS) + "...";
        }
        return "DeepSeek request failed with HTTP " + status + "; response=" + body;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices, String model, Usage usage) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message, @JsonProperty("finish_reason") String finishReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Usage(
            @JsonProperty("prompt_tokens") Long promptTokens,
            @JsonProperty("completion_tokens") Long completionTokens,
            @JsonProperty("total_tokens") Long totalTokens) {
    }
}
