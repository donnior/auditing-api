package com.xingcanai.csqe.auditing.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
        return fetchChatUser(page, limit, null, null);
    }

    /**
     * 获取在读学员课程周序列表（支持按更新时间增量获取）
     *
     * @param page            页码（从 1 开始）
     * @param limit           每页大小（最大 100）
     * @param updateStartTime 更新时间范围开始（可选，需中国时区）
     * @param updateEndTime   更新时间范围结束（可选，需中国时区）
     */
    public Mono<ChatUserResponse> fetchChatUser(Integer page, Integer limit, ZonedDateTime updateStartTime, ZonedDateTime updateEndTime) {
        final int finalPage = (page == null || page < 1) ? 1 : page;
        final int finalLimit = (limit == null) ? 100 : limit;

        ZoneId chinaZone = ZoneId.of("Asia/Shanghai");
        if (updateStartTime != null) {
            updateStartTime = updateStartTime.withZoneSameInstant(chinaZone);
        }
        if (updateEndTime != null) {
            updateEndTime = updateEndTime.withZoneSameInstant(chinaZone);
        }

        logger.info("Fetching chat user page {}... updateStartTime={}, updateEndTime={}",
                finalPage,
                updateStartTime == null ? null : updateStartTime.format(formatter),
                updateEndTime == null ? null : updateEndTime.format(formatter));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("page", finalPage);
        requestBody.put("limit", finalLimit);
        if (updateStartTime != null) {
            requestBody.put("updateStartTime", updateStartTime.format(formatter));
        }
        if (updateEndTime != null) {
            requestBody.put("updateEndTime", updateEndTime.format(formatter));
        }

        return webClient.post()
                .uri(API_USER_URL)
                .header("X-Token", API_TOKEN)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatUserResponse.class)
                .doOnSuccess(response -> {
                    if (response != null && response.getCode() == 200 && response.getData() != null) {
                        // 保底：按更新时间倒序排列，便于分页 + 增量同步场景
                        if (response.getData().getList() != null) {
                            response.getData().getList().sort(
                                    Comparator.comparing(
                                            ChatUserResponse.ChatUserItem::getUpdateTime,
                                            Comparator.nullsLast(Comparator.naturalOrder())
                                    ).reversed()
                            );
                        }
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
