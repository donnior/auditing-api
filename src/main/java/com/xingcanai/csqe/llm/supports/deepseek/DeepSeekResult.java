package com.xingcanai.csqe.llm.supports.deepseek;

public record DeepSeekResult(
        String content,
        String model,
        String finishReason,
        Long promptTokens,
        Long completionTokens,
        Long totalTokens) {
}

