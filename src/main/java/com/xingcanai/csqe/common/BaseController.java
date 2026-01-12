package com.xingcanai.csqe.common;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public interface BaseController {

    public static String DEFAULT_PAGE_SIZE = "10";
    public static String DEFAULT_PAGE = "1";

    default Map<String, Object> fail(String message) {
        return Map.of("success", false, "content", "", "message", message);
    }

    default Map<String, Object> success() {
        return Map.of("success", true);
    }

    default Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    default <T> Page<T> listToPage(List<T> items) {
        return new PageImpl<>(items);
    }
}
