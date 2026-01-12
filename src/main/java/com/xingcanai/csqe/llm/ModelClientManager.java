package com.xingcanai.csqe.llm;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ModelClientManager {

    private List<ModelClient> clients;

    public ModelClientManager(List<ModelClient> clients) {
        this.clients = clients;
    }

    public ModelClient getClient(String model) {
        return this.clients.stream().filter(client -> client.isSupported(model)).findFirst().orElse(null);
    }
}
