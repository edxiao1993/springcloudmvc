package com.kevin.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kevin Chien
 * @version 0.1
 * @date 2024/12/16 18:28
 */
@RestController
public class HelloController {
    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/api/user/hello")
    public String hello() {
        return "hello user";
    }

    @PostMapping("/api/body/hello")
    public String handleMsg(@RequestBody String body) {
        logger.info("msg from request body: {}", body);
        return "I got you msg: " + body;
    }
}
