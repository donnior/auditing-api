package com.example.demo.llm;

public enum SseEventType {

    Reasoning("reasoning"),

    Answering("answering"),

    Failed("failed"),

    End("end"),

    Completed("completed"),

    Usage("usage"),

    Unknown("unknown"),

    Empty("");

    private final String value;

    SseEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
