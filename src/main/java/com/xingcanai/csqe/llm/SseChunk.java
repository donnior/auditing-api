package com.xingcanai.csqe.llm;

import org.springframework.http.codec.ServerSentEvent;

/**
 * 平台统一的SSE事件数据对象。
 */
public record SseChunk(SseEventType event, SseEventData data) {

    public static SseChunk of(SseEventType event, String text) {
        return new SseChunk(event, new SseEventData(event, text));
    }

    public ServerSentEvent<String> toServerSentEvent() {
        return ServerSentEvent.<String>builder().event(this.event.getValue()).data(this.data.toJson()).build();
    }

    public boolean notEmpty() {
        return !SseEventType.Empty.equals(this.event);
    }

    public boolean isNotEmpty() {
        return notEmpty();
    }

    public boolean notUnknown() {
        return !SseEventType.Unknown.equals(this.event);
    }

}
