package com.clipflow.controller;

import com.clipflow.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public ApiResponse<String> hello(
        @RequestParam(defaultValue = "Guest") String name
    ) {
        return ApiResponse.success("Hello, " + name + "!");
    }
}