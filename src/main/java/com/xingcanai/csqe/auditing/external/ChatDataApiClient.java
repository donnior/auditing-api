package com.xingcanai.csqe.auditing.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 外部聊天记录API客户端
 */
@Component
public class ChatDataApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ChatDataApiClient.class);

    private final WebClient webClient;

    private static final String API_DATA_URL = "https://www.cdxwsuger.cn/prod-api/api/wx/getMsgList";
    private static final String API_USER_URL = "https://www.cdxwsuger.cn/prod-api/api/wx/getCardUserList";

    private static final String API_TOKEN = "VoyT09nB2fSlDGbF+NWzPxekKY1ZI/jqDKECSiTK6GoeoyLCARG9SaglnEvSG/WQ";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatDataApiClient() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // Increase buffer size
                .build();
    }

    /**
     * 获取聊天记录数据
     *
     * @param startTime 开始时间, 需要中国时区
     * @param endTime   结束时间, 需要中国时区
     * @param page      页码
     * @param limit     每页大小
     * @return 聊天记录响应
     */
    public Mono<ChatDataResponse> fetchChatData(ZonedDateTime startTime, ZonedDateTime endTime, Integer page, Integer limit) {
        final int finalPage = (page == null || page < 1) ? 1 : page;
        final int finalLimit = (limit == null) ? 100 : limit;


        String startStr = startTime.format(formatter);
        String endStr = endTime.format(formatter);

        // System.out.println("startStr: " + startStr);
        // System.out.println("endStr: " + endStr);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("startDate", startStr);
        requestBody.put("endDate", endStr);
        requestBody.put("page", finalPage);
        requestBody.put("limit", finalLimit);

        logger.info("Fetching chat data with param: {}", requestBody);

        return webClient.post()
                .uri(API_DATA_URL)
                .header("X-Token", API_TOKEN)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatDataResponse.class)
                .doOnSuccess(response -> {
                    if (response != null && response.getCode() == 200 && response.getData() != null) {
                        logger.info("Successfully fetched page {} of chat messages. Total: {}",
                                finalPage, response.getData().getTotal());
                    } else {
                        logger.warn("Fetched chat data but response indicates failure or empty: {}", response);
                    }
                })
                .doOnError(error -> {
                    logger.error("Failed to fetch chat data from external API: {}", error.getMessage());
                });
    }


    public Mono<ChatUserResponse> fetchChatUser(Integer page, Integer limit) {
        final int finalPage = (page == null || page < 1) ? 1 : page;
        final int finalLimit = (limit == null) ? 100 : limit;

        logger.info("Fetching chat user page {}...", finalPage);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("page", finalPage);
        requestBody.put("limit", finalLimit);

        return webClient.post()
                .uri(API_USER_URL)
                .header("X-Token", API_TOKEN)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatUserResponse.class)
                .doOnSuccess(response -> {
                    if (response != null && response.getCode() == 200 && response.getData() != null) {
                        logger.info("Successfully fetched page {} of chat users. Total: {}",
                                finalPage, response.getData().getTotal());
                    } else {
                        logger.warn("Fetched chat user but response indicates failure or empty: {}", response);
                    }
                })
                .doOnError(error -> {
                    logger.error("Failed to fetch chat user from external API: {}", error.getMessage());
                });
    }
}
