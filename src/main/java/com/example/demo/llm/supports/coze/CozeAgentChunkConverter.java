package com.example.demo.llm.supports.coze;

import com.example.demo.llm.supports.AbstractChunkConverter;
import com.example.demo.util.Jsons;

public class CozeAgentChunkConverter extends AbstractChunkConverter {

    /**
        const isEndMessage = (text, event) => text && text.is_end;
const isThinkMessage = (text, event) => event === 'conversation.message.delta' && text.type === 'answer' && text.content_type === 'text' && text.reasoning_content;
const isFailedMessage = (text, event) => event === 'conversation.chat.failed';
const isAnswerMessage = (text, event) => event === 'conversation.message.delta' && text.type === 'answer' && text.content_type === 'text' && text.content;
const answerText = (text, event) => text.type === 'answer' && text.content_type === 'text' && text.content || ''
const reasoningText = (text, event) => text.type === 'answer' && text.content_type === 'text' && text.reasoning_content || ''

    */
    @Override
    public boolean isFailedChunk(String event, String data) {
        return eventEqual(event, "conversation.chat.failed");
    }

    @Override
    public boolean isEndChunk(String event, String data) {
        return "[DONE]".equals(data) || "done".equals(event);
    }

    @Override
    public boolean isAnsweringChunk(String event, String data) {
        return eventEqual(event, "conversation.message.delta")
            && dataEqual(data, "$.type", "answer")
            && dataEqual(data, "$.content_type", "text")
            && notEmpty(data, "$.content");
    }

    @Override
    public boolean isReasoningChunk(String event, String data) {
        return eventEqual(event, "conversation.message.delta")
            && dataEqual(data, "$.type", "answer")
            && dataEqual(data, "$.content_type", "text")
            && notEmpty(data, "$.reasoning_content");
    }

    @Override
    public String answerText(String data) {
        return Jsons.safeRead(data, "$.content");
    }

    @Override
    public String reasoningText(String data) {
        return Jsons.safeRead(data, "$.reasoning_content");
    }
}
