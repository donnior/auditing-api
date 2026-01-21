package com.xingcanai.csqe.auditing.external;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserResponse {

    private Integer code;
    private String msg;
    private Data data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private Long total;
        private List<ChatUserItem> list;
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
    public static class ChatUserItem {
        private String externalUserid;
        private String externalName;
        private String cardName;
        private String userid;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private ZonedDateTime startTime;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private ZonedDateTime createTime;

        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private ZonedDateTime updateTime;

        private Integer weekNumber;
    }
}
