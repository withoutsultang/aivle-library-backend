package com.aivle8.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "프론트 백엔드 크로스~~";
    }
    @GetMapping("/api/hello2")
    public String hello2(){
        return "hello";
    }
}
