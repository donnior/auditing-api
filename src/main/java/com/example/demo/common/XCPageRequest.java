package com.example.demo.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// import io.swagger.annotations.ApiModel;
// import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

// @ApiModel(description = "Page Params")
@Data
/**
 * 1 based page number.
 */
public class XCPageRequest {
    public static final int DefaultPage = 1;
    public static final int DefaultPageSize = 10;

    // @ApiModelProperty(name = "page", value = "Page number", example = "1")
    protected int page = 1;

    // @ApiModelProperty(name = "page_size", value = "Page size", example = "10")
    protected int pageSize = 10;

    public static XCPageRequest of(int page, int pageSize) {
        return new XCPageRequest(page, pageSize, Sort.unsorted());
    }

    protected XCPageRequest(int page, int size, Sort sort) {
        this.page = page;
        this.pageSize = size;
    }

    /**
     * 将当前对象转换为PageRequest对象，注意二者的起始分页不同；PageRequest的起始分页是0，而XCPageRequest的起始分页是1
     *
     */
    public PageRequest toPageRequest() {
        return PageRequest.of(page - 1, pageSize);
    }
}
