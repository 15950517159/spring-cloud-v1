package com.demo.feignClients;

import com.demo.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * value name serviceId都可以指定服务名
 */
@Component
@FeignClient(name = "userserver")
public interface UserService {

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable("id") Long userId);
}

