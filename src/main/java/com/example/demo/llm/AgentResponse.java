package com.example.demo.llm;

import lombok.Data;

@Data
public class AgentResponse {

    private String text;

    public AgentResponse() {}

    public AgentResponse(String text) {
        this.text = text;
    }

}
