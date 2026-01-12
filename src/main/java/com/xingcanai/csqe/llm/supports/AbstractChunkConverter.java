package com.xingcanai.csqe.llm.supports;

import org.springframework.http.codec.ServerSentEvent;

import com.jayway.jsonpath.JsonPath;
import com.xingcanai.csqe.llm.LlmStreamChunkConverter;
import com.xingcanai.csqe.llm.SseChunk;
import com.xingcanai.csqe.llm.SseEventType;
import com.xingcanai.csqe.llm.SseFrame;
import com.xingcanai.csqe.util.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractChunkConverter implements LlmStreamChunkConverter {

    protected static boolean dataEqual(String data, String path, String expected) {
        try {
            return expected.equals(JsonPath.read(data, path));
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean eventEqual(String event, String expected) {
        return expected.equals(event);
    }

    protected boolean notEmpty(String data, String path) {
        try {
            var value = JsonPath.read(data, path);
            return value != null && Strings.isNotEmpty(value.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SseChunk convert(SseFrame message) {

        // log.info("Will convert sse message: '{}'", message);
        var event = message.event();
        String data = message.data();

        log.debug("Will convert sse event: '{}', data: {}", event, data);

        if ("[DONE]".equals(data)) {
            return SseChunk.of(SseEventType.End, "");
        }

        if(isFailedChunk(event, data)) {
            return SseChunk.of(SseEventType.Failed, "");
        }

        if(isEndChunk(event, data)) {
            return SseChunk.of(SseEventType.End, "");
        }
        if(isReasoningChunk(event, data)) {
            return SseChunk.of(SseEventType.Reasoning, reasoningText(data));
        }

        if(isAnsweringChunk(event, data)) {
            return SseChunk.of(SseEventType.Answering, answerText(data));
        }

        log.debug("Unknown sse event: '{}', data: {}", event, data);
        return SseChunk.of(SseEventType.Unknown, "");
    }

    public abstract boolean isFailedChunk(String event, String data);

    public abstract boolean isEndChunk(String event, String data);

    public abstract boolean isAnsweringChunk(String event, String data);

    public abstract boolean isReasoningChunk(String event, String data);

    public abstract String answerText(String data);

    public abstract String reasoningText(String data);
}
