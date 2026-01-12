package com.xingcanai.csqe.auditing.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 外部聊天记录接口响应数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDataResponse {

    private Integer code;
    private String msg;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long total;
        private List<ChatMessageItem> list;
        private Integer pageNum;
        private Integer pageSize;
        private Integer size;
        private Integer startRow;
        private Integer endRow;
        private Integer pages;
        private Integer prePage;
        private Integer nextPage;
        private Boolean isFirstPage;
        private Boolean isLastPage;
        private Boolean hasPreviousPage;
        private Boolean hasNextPage;
        private Integer navigatePages;
        private List<Integer> navigatepageNums;
        private Integer navigateFirstPage;
        private Integer navigateLastPage;
    }

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageItem {
        private String msgId;
        private String fromId;
        private String fromName;
        private String acceptId;
        private Integer acceptType;
        private String acceptName;
        private String msgType;
        private String content;
        private Long dataSeq;
        // API returns "2025-11-19 23:38:20", so we might need custom deserializer or String then parse
        // Using String for now to be safe, or Jackson format
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private ZonedDateTime msgTime;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private ZonedDateTime createTime;
    }
}
