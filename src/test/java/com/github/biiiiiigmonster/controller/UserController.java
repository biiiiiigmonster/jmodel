package com.github.biiiiiigmonster.controller;

import com.github.biiiiiigmonster.entity.User;
import com.github.biiiiiigmonster.router.PathModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @RequestMapping("/users/{user}")
    public User user(@PathModel User user) {
        return user;
    }
}
