package com.aivle8.backend.demo;

import com.aivle8.backend.BackendApplication;
import org.springframework.boot.SpringApplication;

/** Separate source set: this entry point is not packaged for AWS deployment. */
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
