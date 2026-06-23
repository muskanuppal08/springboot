package com.example.CRUD.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/ping-json")
    public Map<String, Object> pingJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "UP");
        map.put("message", "Service is running");
        return map;
    }
}
