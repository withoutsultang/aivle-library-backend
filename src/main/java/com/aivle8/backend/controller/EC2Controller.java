package com.aivle8.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EC2Controller {

    @GetMapping("/")
    public String healthCheck() {
        return "The backend server is running!";
    }
}