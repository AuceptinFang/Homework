package com.example.homeworksystem.controller;

import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.UserService;
import com.example.homeworksystem.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名不能为空"));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "密码不能为空"));
        }

        try {
            User user = userService.createUser(username, password);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册成功",
                "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "用户名不能为空"
            ));
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "密码不能为空"
            ));
        }

        try {
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (user.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "首次登录请先注册设置密码"
                ));
            }

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username.trim(), password.trim())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("username", username);
            response.put("isAdmin", isAdmin);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "登录失败：" + e.getMessage()
            ));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "未登录"));
        }
        
        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
            
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("isAdmin", user.isAdmin());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usernames")
    public ResponseEntity<?> getUsernames() {
        return ResponseEntity.ok(Arrays.asList(
            "admin", // 管理员账号放在最前面
            "段鹏鹏", "方震", "高天全", "高子涵", "龚皓",
            "霍雨婷", "贾奇燠", "贾一丁", "金闿翎", "靳宇晨",
            "李希", "刘森源", "刘文晶", "吕彦儒", "吕泽熙",
            "苏宗佑", "汤锦健", "王奕涵", "王悦琳", "王志成",
            "王墨", "王泽君", "夏正鑫", "肖俊波", "杨海屹",
            "杨孟涵", "张成宇", "张峻菘", "张钟文", "赵萌"
        ));
    }
} 