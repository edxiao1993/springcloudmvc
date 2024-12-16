package com.kevin.interfaces;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kevin Chien
 * @version 0.1
 * @date 2024/12/16 18:28
 */
@RestController
public class HelloController {

    @GetMapping("/api/user/hello")
    public String hello() {
        return "hello user";
    }
}
