package com.xingcanai.csqe.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Main {

    @GetMapping("/")
    public String hello() {
        return "Hello, World!";
    }

}
