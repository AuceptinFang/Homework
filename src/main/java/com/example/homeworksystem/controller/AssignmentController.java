package com.example.homeworksystem.controller;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.AssignmentService;
import com.example.homeworksystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final UserService userService;

    public AssignmentController(AssignmentService assignmentService, UserService userService) {
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<Assignment>> getAssignments(@RequestParam(required = false) Boolean onlyAvailable) {
        try {
            if (Boolean.TRUE.equals(onlyAvailable)) {
                return ResponseEntity.ok(assignmentService.getAvailableAssignments());
            }
            return ResponseEntity.ok(assignmentService.getAllAssignments());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createAssignment(
        @RequestBody Map<String, Object> request,
        Authentication authentication
    ) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "请先登录"));
            }

            String title = (String) request.get("title");
            String description = (String) request.get("description");
            String deadlineStr = (String) request.get("deadline");
            Boolean allowSubmit = (Boolean) request.get("allowSubmit");
            String fileFormat = (String) request.get("fileFormat");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "作业标题不能为空"));
            }

            LocalDateTime deadline = null;
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                try {
                    deadline = LocalDateTime.parse(deadlineStr);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "截止日期格式错误"));
                }
            }

            // 获取当前用户
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!user.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "只有管理员可以发布作业"));
            }

            Assignment assignment = assignmentService.createAssignment(
                title,
                description,
                deadline,
                user,
                fileFormat != null ? fileFormat : "pdf"
            );
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "发布作业失败：" + e.getMessage()));
        }
    }

    @PutMapping("/{id}/submit-control")
    public ResponseEntity<?> updateSubmitControl(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> request,
        Authentication authentication
    ) {
        try {
            // 检查权限
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!user.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "只有管理员可以修改提交控制"));
            }

            Assignment assignment = assignmentService.updateSubmissionControl(id, request.get("allowSubmit"));
            return ResponseEntity.ok(assignment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "更新提交控制失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(
        @PathVariable Long id,
        Authentication authentication
    ) {
        try {
            // 检查权限
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!user.isAdmin()) {
                return ResponseEntity.status(403).body(Map.of("error", "只有管理员可以删除作业"));
            }

            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "删除作业失败：" + e.getMessage()));
        }
    }
} 