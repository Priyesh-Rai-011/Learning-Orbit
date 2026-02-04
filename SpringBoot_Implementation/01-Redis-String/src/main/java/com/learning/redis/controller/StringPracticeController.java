package com.learning.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redis")
public class StringPracticeController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "Key " + key + " set successfully!";
    }

    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        return "Value for " + key + " is: " + redisTemplate.opsForValue().get(key);
    }
}

// ----