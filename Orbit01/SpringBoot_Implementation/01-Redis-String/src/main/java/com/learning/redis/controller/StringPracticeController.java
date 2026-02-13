// package com.learning.redis.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/redis")
// public class StringPracticeController {

//     @Autowired
//     private StringRedisTemplate redisTemplate;

//     @GetMapping("/set")
//     public String setValue(@RequestParam String key, @RequestParam String value) {
//         redisTemplate.opsForValue().set(key, value);
//         // redisTemplate.opsForValue().set("name","Priyesh");
//         return "Key " + key + " set successfully!";
//     }

//     @GetMapping("/get")
//     public String getValue(@RequestParam String key) {
//         return "Value for " + key + " is: " + redisTemplate.opsForValue().get(key);
//     }
// }

// // ----









package com.learning.redis.controller;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// import javax.tools.StandardLocation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/string")
public class StringPracticeController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // -------------------- SET --------------------
    // Example: POST /api/string/set?key=name&value=priyesh&ttl=60
    @PostMapping("/set")
    public ResponseEntity<Map<String, Object>> setValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) Long ttl
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (ttl != null && ttl <= 0) {
            response.put("status", "error");
            response.put("message", "TTL must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }

        if (ttl != null) {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
        } else {
            redisTemplate.opsForValue().set(key, value);
        }

        response.put("status", "success");
        response.put("operation", "SET");
        response.put("key", key);
        response.put("value", value);
        response.put("ttlSeconds", ttl == null ? "no-expiry" : ttl);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------- GET --------------------
    // Example: GET /api/string/get?key=name
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getValue(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            response.put("status", "error");
            response.put("operation", "GET");
            response.put("key", key);
            response.put("message", "Key not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("status", "success");
        response.put("operation", "GET");
        response.put("key", key);
        response.put("value", value);

        return ResponseEntity.ok(response);
    }

    // -------------------- DELETE --------------------
    // Example: DELETE /api/string/delete?key=name
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteKey(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        Boolean deleted = redisTemplate.delete(key);

        response.put("status", "success");
        response.put("operation", "DELETE");
        response.put("key", key);
        response.put("deleted", deleted);

        return ResponseEntity.ok(response);
    }

    // -------------------- MSET --------------------
    // Example JSON:
    // {
    //   "user1": "Priyesh",
    //   "user2": "Rahul"
    // }
    @PostMapping("/mset")
    public ResponseEntity<Map<String, Object>> multiSet(@RequestBody Map<String, String> data) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (data == null || data.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Request body is empty");
            return ResponseEntity.badRequest().body(response);
        }

        redisTemplate.opsForValue().multiSet(data);

        response.put("status", "success");
        response.put("operation", "MSET");
        response.put("keysInserted", data.size());
        response.put("data", data);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------- MGET --------------------
    // Example JSON:
    // ["user1", "user2"]
    @PostMapping("/mget")
    public ResponseEntity<Map<String, Object>> multiGet(@RequestBody List<String> keys) {

        Map<String, Object> response = new LinkedHashMap<>();

        if (keys == null || keys.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Keys list is empty");
            return ResponseEntity.badRequest().body(response);
        }

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            result.put(keys.get(i), values.get(i));
        }

        response.put("status", "success");
        response.put("operation", "MGET");
        response.put("result", result);

        return ResponseEntity.ok(response);
    }

    // -------------------- INCR --------------------
    @PostMapping("/incr")
    public ResponseEntity<Map<String, Object>> incr(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        Long newValue = redisTemplate.opsForValue().increment(key);

        response.put("status", "success");
        response.put("operation", "INCR");
        response.put("key", key);
        response.put("newValue", newValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- INCRBY --------------------
    @PostMapping("/incrby")
    public ResponseEntity<Map<String, Object>> incrBy(
            @RequestParam String key,
            @RequestParam Long value
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        Long newValue = redisTemplate.opsForValue().increment(key, value);

        response.put("status", "success");
        response.put("operation", "INCRBY");
        response.put("key", key);
        response.put("incrementBy", value);
        response.put("newValue", newValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- INCRBYFLOAT --------------------
    @PostMapping("/incrbyfloat")
    public ResponseEntity<Map<String, Object>> incrByFloat(
            @RequestParam String key,
            @RequestParam Double value
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        Double newValue = redisTemplate.opsForValue().increment(key, value);

        response.put("status", "success");
        response.put("operation", "INCRBYFLOAT");
        response.put("key", key);
        response.put("incrementBy", value);
        response.put("newValue", newValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- DECR --------------------
    @PostMapping("/decr")
    public ResponseEntity<Map<String, Object>> decr(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        Long newValue = redisTemplate.opsForValue().decrement(key);

        response.put("status", "success");
        response.put("operation", "DECR");
        response.put("key", key);
        response.put("newValue", newValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- DECRBY --------------------
    @PostMapping("/decrby")
    public ResponseEntity<Map<String, Object>> decrBy(
            @RequestParam String key,
            @RequestParam Long value
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        Long newValue = redisTemplate.opsForValue().decrement(key, value);

        response.put("status", "success");
        response.put("operation", "DECRBY");
        response.put("key", key);
        response.put("decrementBy", value);
        response.put("newValue", newValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- TTL --------------------
    // Returns TTL in seconds (-1 means no expiry, -2 means key does not exist)
    @GetMapping("/ttl")
    public ResponseEntity<Map<String, Object>> ttl(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        Long ttl = redisTemplate.getExpire(key);

        response.put("status", "success");
        response.put("operation", "TTL");
        response.put("key", key);
        response.put("ttlSeconds", ttl);

        return ResponseEntity.ok(response);
    }

    // -------------------- EXPIRE --------------------
    // Example: POST /api/string/expire?key=name&ttl=60
    @PostMapping("/expire")
    public ResponseEntity<Map<String, Object>> expire(
            @RequestParam String key,
            @RequestParam Long ttl
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (ttl <= 0) {
            response.put("status", "error");
            response.put("message", "TTL must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }

        Boolean result = redisTemplate.expire(key, Duration.ofSeconds(ttl));

        response.put("status", "success");
        response.put("operation", "EXPIRE");
        response.put("key", key);
        response.put("ttlSeconds", ttl);
        response.put("expireApplied", result);

        return ResponseEntity.ok(response);
    }

    // -------------------- SETBIT --------------------
    @PostMapping("/setbit")
    public ResponseEntity<Map<String, Object>> setBit(
            @RequestParam String key,
            @RequestParam Long offset,
            @RequestParam Boolean bitValue
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        redisTemplate.opsForValue().setBit(key, offset, bitValue);

        response.put("status", "success");
        response.put("operation", "SETBIT");
        response.put("key", key);
        response.put("offset", offset);
        response.put("bitValue", bitValue);

        return ResponseEntity.ok(response);
    }

    // -------------------- GETBIT --------------------
    @GetMapping("/getbit")
    public ResponseEntity<Map<String, Object>> getBit(
            @RequestParam String key,
            @RequestParam Long offset
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        Boolean bit = redisTemplate.opsForValue().getBit(key, offset);

        response.put("status", "success");
        response.put("operation", "GETBIT");
        response.put("key", key);
        response.put("offset", offset);
        response.put("bitValue", bit);

        return ResponseEntity.ok(response);
    }

    // -------------------- BITCOUNT --------------------
    @GetMapping("/bitcount")
    public ResponseEntity<Map<String, Object>> bitCount(@RequestParam String key) {

        Map<String, Object> response = new LinkedHashMap<>();

        Long count = redisTemplate.execute((RedisCallback<Long>) connection ->
                // connection.bitCount(key.getBytes(StandardCharsets.UTF_8))
                connection.stringCommands().bitCount(key.getBytes(StandardCharsets.UTF_8))
        );

        response.put("status", "success");
        response.put("operation", "BITCOUNT");
        response.put("key", key);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}
