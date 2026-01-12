package com.xingcanai.csqe.llm.supports.coze;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "llm.provider.coze")
@Data
public class CozeConfigProperties {

    private String apikey;
    private String apibase = "https://api.coze.cn";

    private String appId;
    // private String privateKeyPath;
    // private String publicKey;

}
