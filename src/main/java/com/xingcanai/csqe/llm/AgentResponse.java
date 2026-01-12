package com.xingcanai.csqe.llm;

import lombok.Data;

@Data
public class AgentResponse {

    private String text;

    public AgentResponse() {}

    public AgentResponse(String text) {
        this.text = text;
    }

}
