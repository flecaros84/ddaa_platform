package com.ddaa.authservice.controller;

import com.ddaa.authservice.dto.CreateUserRequest;
import com.ddaa.authservice.model.User;
import com.ddaa.authservice.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
                "service", "auth-service",
                "status", "ok"
        );
    }

    @GetMapping("/login")
    public Map<String, String> login() {
        return Map.of(
                "message", "Use /oauth2/authorization/google to sign in with Google"
        );
    }

    @GetMapping("/error")
    public Map<String, String> error() {
        return Map.of(
                "authenticated", "false",
                "message", "Authentication failed or unauthorized domain"
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (principal == null) {
            response.put("authenticated", false);
            return response;
        }

        String email = principal.getAttribute("email");
        Optional<User> userOpt = userService.findByEmail(email);

        response.put("authenticated", true);
        response.put("name", principal.getAttribute("name"));
        response.put("email", email);
        response.put("googleId", principal.getAttribute("sub"));
        response.put("domain", principal.getAttribute("hd"));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            response.put("id", user.getId());
            response.put("role", user.getRole());
            response.put("active", user.getActive());
            response.put("lastLogin", user.getLastLogin());
        }

        return response;
    }

    @PostMapping("/users/test")
    public User createTestUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}