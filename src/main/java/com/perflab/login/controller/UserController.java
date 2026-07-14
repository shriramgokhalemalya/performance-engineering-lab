package com.perflab.login.controller;

import com.perflab.login.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/users", "/api/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        return Map.of("username", authentication.getName());
    }

    @GetMapping("/all")
    public List<Map<String, Object>> findAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addUser(@RequestBody Map<String, String> request) {
        return userService.addUser(request.get("username"));
    }
}
