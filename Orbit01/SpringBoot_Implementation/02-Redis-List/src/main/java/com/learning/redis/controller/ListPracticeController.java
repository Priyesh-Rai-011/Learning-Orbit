package com.learning.redis.controller;

import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/list")
public class ListPracticeController {

    private final StringRedisTemplate redisTemplate;

    public ListPracticeController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 1. KEYS: Find all keys matching a pattern (e.g., /keys?pattern=sent*)
    @GetMapping("/keys")
    public Set<String> getKeys(@RequestParam String pattern) {
        return redisTemplate.keys(pattern);
    }

    // 2. LPUSH: Add to head
    @PostMapping("/lpush/{key}")
    public Long lpush(@PathVariable String key, @RequestParam String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    // 3. LRANGE: Get range (e.g., /range/sentence?start=0&end=-1)
    @GetMapping("/range/{key}")
    public List<String> getRange(@PathVariable String key, @RequestParam int start, @RequestParam int end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // 4. RPUSH: Add to tail
    @PostMapping("/rpush/{key}")
    public Long rpush(@PathVariable String key, @RequestParam String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    // 5. LLEN: Get length
    @GetMapping("/size/{key}")
    public Long getSize(@PathVariable String key) {
        return redisTemplate.opsForList().size(key);
    }

    // 6. LPOP: Remove from head
    @DeleteMapping("/lpop/{key}")
    public String lpop(@PathVariable String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // 7. RPOP: Remove from tail
    @DeleteMapping("/rpop/{key}")
    public String rpop(@PathVariable String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    // 8. LSET: Set value at index
    @PutMapping("/lset/{key}")
    public String lset(@PathVariable String key, @RequestParam int index, @RequestParam String value) {
        redisTemplate.opsForList().set(key, index, value);
        return "Success";
    }

    // 9. LINSERT: Insert before or after a pivot
    @PostMapping("/linsert/{key}")
    public Long linsert(@PathVariable String key, @RequestParam String pivot, @RequestParam String value, @RequestParam boolean before) {
        if (before) {
            return redisTemplate.opsForList().leftPush(key, pivot, value);
        } else {
            return redisTemplate.opsForList().rightPush(key, pivot, value);
        }
    }

    // 10. LINDEX: Get element by index
    @GetMapping("/lindex/{key}/{index}")
    public String lindex(@PathVariable String key, @PathVariable int index) {
        return redisTemplate.opsForList().index(key, index);
    }

    // 11. LPUSHX: Push head only if exists
    @PostMapping("/lpushx/{key}")
    public Long lpushx(@PathVariable String key, @RequestParam String value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    // 12. RPUSHX: Push tail only if exists
    @PostMapping("/rpushx/{key}")
    public Long rpushx(@PathVariable String key, @RequestParam String value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    // 13. SORT: Sort the list (Alpha)
    @GetMapping("/sort/{key}")
    public List<String> sort(@PathVariable String key) {
        org.springframework.data.redis.core.query.SortQuery<String> query = 
            org.springframework.data.redis.core.query.SortQueryBuilder.sort(key).alphabetical(true).build();
        return redisTemplate.sort(query);
    }

    // 14. LREM: Remove X occurrences of value
    @DeleteMapping("/lrem/{key}")
    public Long lrem(@PathVariable String key, @RequestParam int count, @RequestParam String value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    // 16. LTRIM: Keep only specified range
    @PostMapping("/ltrim/{key}")
    public String ltrim(@PathVariable String key, @RequestParam int start, @RequestParam int end) {
        redisTemplate.opsForList().trim(key, start, end);
        return "Trimmed";
    }

    // 17. BLPOP: Blocking Pop Left (waits X seconds)
    @DeleteMapping("/blpop/{key}")
    public String blpop(@PathVariable String key, @RequestParam int timeout) {
        return redisTemplate.opsForList().leftPop(key, Duration.ofSeconds(timeout));
    }

    // 18. BRPOP: Blocking Pop Right (waits X seconds)
    @DeleteMapping("/brpop/{key}")
    public String brpop(@PathVariable String key, @RequestParam int timeout) {
        return redisTemplate.opsForList().rightPop(key, Duration.ofSeconds(timeout));
    }

    // 19. BLMOVE: Block and move between lists
    @PostMapping("/blmove")
    public String blmove(@RequestParam String source, @RequestParam String destination) {
        return redisTemplate.opsForList().move(source, RedisListCommands.Direction.LEFT, destination, RedisListCommands.Direction.RIGHT, Duration.ofSeconds(10));
    }

    // 20. LMPOP: Pop multiple elements (Requires Redis 7.0+)
    @DeleteMapping("/lmpop/{key}")
    public List<String> lmpop(@PathVariable String key, @RequestParam int count) {
        return redisTemplate.opsForList().leftPop(key, count);
    }
}