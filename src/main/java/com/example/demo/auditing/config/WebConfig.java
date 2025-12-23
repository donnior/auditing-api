package com.example.demo.auditing.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // argumentResolvers.add(new AuthenticatedObjectArgumentResolver());
        argumentResolvers.add(new PageRequestArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // registry.addInterceptor(tokenAuthenticateFilter)
        //     .addPathPatterns("/**")
        //     .excludePathPatterns("/auth", "/internal/**", "/swagger-resources/**",
        //         "/v2/api-docs/**","/swagger-ui/**", "/webjars/**");
    }

}
