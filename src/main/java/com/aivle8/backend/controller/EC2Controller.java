package com.aivle8.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EC2Controller {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}