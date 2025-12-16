package com.github.biiiiiigmonster.controller;

import com.github.biiiiiigmonster.entity.Post;
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

    @RequestMapping("/users/byName/{user}")
    public User userByName(@PathModel(routeKey = "name") User user) {
        return user;
    }

    @RequestMapping("/users/{user}/posts/{post}")
    public Object[] multiModel(@PathModel User user, @PathModel Post post) {
        return new Object[]{user, post};
    }

    @RequestMapping("/users/{user}/posts/{post}/scopeBinding")
    public Object[] multiScopeBindingModel(@PathModel User user, @PathModel(scopeBinding = true) Post post) {
        return new Object[]{user, post};
    }
}
