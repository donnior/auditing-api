package com.xingcanai.csqe.auditing.config;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.xingcanai.csqe.common.XCPageRequest;
import com.xingcanai.csqe.util.Strings;

public class PageRequestArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(XCPageRequest.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        int page = XCPageRequest.DefaultPage;
        int pageSize = XCPageRequest.DefaultPageSize;

        String pageStr = webRequest.getParameter("page");
        String pageSizeStr1 = webRequest.getParameter("page_size");
        String pageSizeStr2 = webRequest.getParameter("pageSize");

        if (Strings.isNotEmpty(pageStr)) {
            page = Integer.parseInt(pageStr);
        }

        if (Strings.isNotEmpty(pageSizeStr1)) {
            pageSize = Integer.parseInt(pageSizeStr1);
        }

        if (Strings.isNotEmpty(pageSizeStr2)) {
            pageSize = Integer.parseInt(pageSizeStr2);
        }
        return XCPageRequest.of(page, pageSize);
    }
}
