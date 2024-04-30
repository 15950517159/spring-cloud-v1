package com.demo.controller;

import com.demo.domain.Order;
import com.demo.domain.User;
import com.demo.feignClients.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id") Long orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setDesc("hello kit");
        User user = userService.getUser(orderId);
        order.setUser(user);
        return order;
    }


//    @Autowired
//    private RestTemplate restTemplate;
//
//    @GetMapping("/{id}")
//    public Order getOrder(@PathVariable("id") String orderId) {
//        Order order = new Order();
//        order.setOrderId(orderId);
//        order.setDesc("黑丝");
//        User user = restTemplate.getForObject("http://userserver/user/" + Long.valueOf(orderId), User.class);
//        order.setUser(user);
//        return order;
//    }

    @GetMapping("/discoveryClient")
    public Object discoveryClient() {
        List<String> services = discoveryClient.getServices();
        return services;
    }

    @GetMapping("/tryLock")
    public Object tryLockRedis() {
        tryLock("key3", 0, 30L);
        return new Object();
    }

    @GetMapping("/lock")
    public Object lockRedis() {
        lock("key3", 0);
        return new Object();
    }

    private boolean lock(String key, Integer val) {
        StringBuffer script = new StringBuffer();
        script.append("local key= KEYS[1] ");
        script.append("local value = ARGV[1] ");
        script.append("if redis.call('get',key) == value then ");
        script.append("     redis.call('del',key)");
        script.append("     return 1 ");
        script.append("else ");
        script.append("     return 0 ");
        script.append(" end ");
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(), Long.class);
        List<String> keys = Arrays.asList(key);
        Long result = (Long) redisTemplate.execute(redisScript, keys, val);
        System.out.println(result);
        return 1 == result ? true : false;
    }

    private boolean tryLock(String key, Integer val, Long expire) {
        StringBuffer script = new StringBuffer();
        script.append("local key=KEYS[1] ");
        script.append("local value = ARGV[1] ");
        script.append("local expire = ARGV[2] ");
        script.append("if redis.call('setnx',key,value)==1 then ");
        script.append("     redis.call('expire',key,expire)");
        script.append("     return 1 ");
        script.append("else ");
        script.append("     return 0 ");
        script.append(" end ");
        RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(), Long.class);
        List<String> keys = Arrays.asList(key);
        Long result = (Long) redisTemplate.execute(redisScript, keys, val, expire);
        System.out.println(result);
        return 1 == result ? true : false;
    }

    public void executeLuaScript() {
        String script = "local key = KEYS[1] local value = ARGV[1] local expire = ARGV[2] " +
                "if (redis.call('exists', key) == 0) then " +
                "    redis.call('hset', key, value, value) " +
                "    redis.call('pexpire', key, expire) " +
                "    return 1 " +
                "elseif (redis.call('hexists', key, value) == 1) then " +
                "    redis.call('hincrby', key, value, 1) " +
                "    redis.call('pexpire', key, expire) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                " end ";

        RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        List<String> keys = Arrays.asList("key1");
        String value = System.currentTimeMillis() + "";
        Long result = (Long) redisTemplate.execute(redisScript, keys, value, 600000);
        System.out.println(result);
    }

    public static void sort(int[] a, int low, int high) {
        int start = low;
        int end = high;
        int key = a[low];
        while (end > start) {
            while (end > start && a[end] >= key) {
                end--;
            }

            if (a[end] <= key) {
                int temp = a[end];
                a[end] = a[start];
                a[start] = temp;
            }
            while (end > start && a[start] <= key) {
                start++;
            }
            if (a[start] >= key) {
                int temp = a[start];
                a[start] = a[end];
                a[end] = temp;
            }
        }
        System.out.printf("JSON=");
        if (start > low) sort(a, low, start - 1);//左边序列。第一个索引位置到关键值索引-1
        if (end < high) sort(a, end + 1, high);//右边序列。从关键值索引+1 到最后一个
        System.out.printf("JSON2");
    }

    public static void main(String[] args) {
        int[] a = {12, 20, 5, 16, 15, 1, 30, 45};
        sort(a, 0, a.length - 1);
    }
}