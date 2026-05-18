package com.xingcanai.csqe.auditing.external;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampCustomerDailyPerformanceResponse {

    private Integer code;
    private String msg;
    private ResponseData data;

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseData {
        private Long total;
        private List<CampCustomerDailyPerformanceItem> list;
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
    public static class CampCustomerDailyPerformanceItem {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate statDate;
        private String campTag;
        private String sysUserId;
        private String salesName;
        private String groupName;
        private String externalUserid;
        private String externalName;
        private BigDecimal gmvAmount;
        private BigDecimal refundAmount;
    }
}
