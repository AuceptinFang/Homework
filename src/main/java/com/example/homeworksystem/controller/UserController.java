package com.example.homeworksystem.controller;

import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有学生用户
     * @param authentication 认证信息
     * @return 学生用户列表
     */
    @GetMapping
    public ResponseEntity<?> getAllStudents(Authentication authentication) {
        // 检查权限
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "只有管理员可以查看所有用户"));
        }

        List<User> students = userService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 请求体
     * @param authentication 认证信息
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
        @PathVariable Long userId,
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        // 检查权限
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
        }

        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 只有管理员或用户本人可以修改信息
        if (!currentUser.isAdmin() && !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权修改其他用户信息"));
        }

        try {
            // 获取要修改的用户
            User userToUpdate = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 更新用户信息
            String realName = request.get("realName");
            String studentId = request.get("studentId");

            if (realName != null && !realName.trim().isEmpty()) {
                userToUpdate.setRealName(realName.trim());
            }

            if (studentId != null && !studentId.trim().isEmpty()) {
                userToUpdate.setStudentId(studentId.trim());
            }

            // 保存更新后的用户信息
            User updatedUser = userService.saveUser(userToUpdate);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("realName", updatedUser.getRealName());
            response.put("studentId", updatedUser.getStudentId());
            response.put("isAdmin", updatedUser.isAdmin());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "更新用户信息失败：" + e.getMessage()));
        }
    }

    /**
     * 批量导入学生信息
     * @param request 请求体，包含学生信息列表
     * @param authentication 认证信息
     * @return 导入结果
     */
    @PostMapping("/batch-import")
    public ResponseEntity<?> batchImportStudents(
        @RequestBody List<Map<String, String>> request,
        Authentication authentication
    ) {
        // 检查权限
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "只有管理员可以批量导入学生信息"));
        }

        try {
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (Map<String, String> studentInfo : request) {
                String studentUsername = studentInfo.get("username");
                String realName = studentInfo.get("realName");
                String studentId = studentInfo.get("studentId");

                if (studentUsername == null || studentUsername.trim().isEmpty()) {
                    failCount++;
                    errorMessages.append("用户名不能为空; ");
                    continue;
                }

                try {
                    // 检查用户是否存在
                    User existingUser = userService.findByUsername(studentUsername)
                        .orElse(null);

                    if (existingUser == null) {
                        // 创建新用户，初始密码为空
                        existingUser = userService.createUser(studentUsername, null);
                    }

                    // 更新用户信息
                    if (realName != null && !realName.trim().isEmpty()) {
                        existingUser.setRealName(realName.trim());
                    }
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        existingUser.setStudentId(studentId.trim());
                    }

                    userService.saveUser(existingUser);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errorMessages.append(studentUsername).append(": ").append(e.getMessage()).append("; ");
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            if (failCount > 0) {
                response.put("errorMessages", errorMessages.toString());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "批量导入学生信息失败：" + e.getMessage()));
        }
    }
} 