package com.xingcanai.csqe.llm.supports.deepseek;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepSeekClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Queue<TestResponse> responses = new ArrayDeque<>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    private HttpServer server;
    private DeepSeekProperties properties;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", this::handleRequest);
        server.start();

        properties = new DeepSeekProperties();
        properties.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
        properties.setApiKey("test-secret");
        properties.setModel("deepseek-v4-pro");
        properties.setTimeout(Duration.ofSeconds(2));
        properties.setRetryDelay(Duration.ofMillis(1));
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsExpectedRequestAndReturnsMarkdownWithoutValidation() throws Exception {
        responses.add(new TestResponse(200, """
            {
              "choices": [{
                "message": {"content": "# 只有一个章节\\n数字也可能不合理"},
                "finish_reason": "length"
              }],
              "model": "deepseek-v4-pro",
              "usage": {
                "prompt_tokens": 123,
                "completion_tokens": 45,
                "total_tokens": 168
              }
            }
            """, 0));

        DeepSeekResult result = client().complete("system rules", "raw records");

        assertEquals("# 只有一个章节\n数字也可能不合理", result.content());
        assertEquals("length", result.finishReason());
        assertEquals(123L, result.promptTokens());
        assertEquals(45L, result.completionTokens());
        assertEquals(168L, result.totalTokens());
        assertEquals("Bearer test-secret", authorization.get());

        JsonNode request = objectMapper.readTree(requestBody.get());
        assertEquals("deepseek-v4-pro", request.get("model").asText());
        assertEquals("enabled", request.at("/thinking/type").asText());
        assertEquals("high", request.get("reasoning_effort").asText());
        assertEquals(0.2, request.get("temperature").asDouble());
        assertEquals(32768, request.get("max_tokens").asInt());
        assertFalse(request.get("stream").asBoolean());
        assertEquals("system rules", request.at("/messages/0/content").asText());
        assertEquals("raw records", request.at("/messages/1/content").asText());
        assertFalse(request.has("response_format"));
    }

    @Test
    void retries429TwiceThenReturnsSuccess() {
        responses.add(new TestResponse(429, "{}", 0));
        responses.add(new TestResponse(429, "{}", 0));
        responses.add(successResponse("ok"));

        DeepSeekResult result = client().complete("system", "user");

        assertEquals("ok", result.content());
        assertEquals(3, requestCount.get());
    }

    @Test
    void doesNotRetry401() {
        responses.add(new TestResponse(401, "{}", 0));

        DeepSeekException exception = assertThrows(
                DeepSeekException.class,
                () -> client().complete("system", "user"));

        assertEquals("HTTP_401", exception.getErrorCode());
        assertEquals(1, requestCount.get());
    }

    @Test
    void includesProviderResponseWhenRequestIsRejected() {
        responses.add(new TestResponse(400, """
            {
              "error": {
                "message": "Input tokens exceed the model context limit",
                "type": "invalid_request_error",
                "code": "invalid_request_error"
              }
            }
            """, 0));

        DeepSeekException exception = assertThrows(
                DeepSeekException.class,
                () -> client().complete("system", "user"));

        assertEquals("HTTP_400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Input tokens exceed the model context limit"));
        assertEquals(1, requestCount.get());
    }

    @Test
    void reportsTimeoutWithoutRetrying() {
        DeepSeekClient deepSeekClient = client();
        responses.add(successResponse("warm"));
        deepSeekClient.complete("system", "warmup");
        requestCount.set(0);

        properties.setTimeout(Duration.ofMillis(30));
        responses.add(new TestResponse(200, successResponseBody("late"), 150));

        DeepSeekException exception = assertThrows(
                DeepSeekException.class,
                () -> deepSeekClient.complete("system", "user"));

        assertEquals("TIMEOUT", exception.getErrorCode());
        assertEquals(1, requestCount.get());
        assertTrue(exception.getMessage().contains("timed out"));
    }

    private DeepSeekClient client() {
        return new DeepSeekClient(WebClient.builder(), properties);
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        requestCount.incrementAndGet();
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        TestResponse response = responses.remove();
        if (response.delayMillis() > 0) {
            try {
                Thread.sleep(response.delayMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(response.status(), body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private TestResponse successResponse(String content) {
        return new TestResponse(200, successResponseBody(content), 0);
    }

    private String successResponseBody(String content) {
        return """
            {
              "choices": [{
                "message": {"content": "%s"},
                "finish_reason": "stop"
              }],
              "model": "deepseek-v4-pro",
              "usage": {
                "prompt_tokens": 1,
                "completion_tokens": 1,
                "total_tokens": 2
              }
            }
            """.formatted(content);
    }

    private record TestResponse(int status, String body, long delayMillis) {
    }
}
