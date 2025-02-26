package com.example.homeworksystem.controller;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.Submission;
import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.AssignmentService;
import com.example.homeworksystem.service.SubmissionService;
import com.example.homeworksystem.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    private final AssignmentService assignmentService;
    private final UserService userService;

    public SubmissionController(
        SubmissionService submissionService,
        AssignmentService assignmentService,
        UserService userService
    ) {
        this.submissionService = submissionService;
        this.assignmentService = assignmentService;
        this.userService = userService;
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(
        @PathVariable Long assignmentId,
        Authentication authentication
    ) {
        try {
            // 检查权限
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (!user.isAdmin()) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "没有权限查看提交情况"));
            }

            Assignment assignment = assignmentService.getAssignment(assignmentId);
            if (assignment == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "作业不存在"));
            }

            List<Submission> submissions = submissionService.getSubmissionsByAssignment(assignment);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Submission>> getSubmissionsByUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 如果是管理员，返回所有提交记录
            if (user.isAdmin()) {
                return ResponseEntity.ok(submissionService.getSubmissionsByUser(null));
            }
            
            // 如果是普通用户，只返回自己的提交记录
            return ResponseEntity.ok(submissionService.getSubmissionsByUser(user));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> submitHomework(
        @PathVariable Long assignmentId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String description,
        Authentication authentication
    ) {
        try {
            System.out.println("开始处理文件上传请求...");
            System.out.println("作业ID: " + assignmentId);
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize());
            System.out.println("文件类型: " + file.getContentType());
            
            String username = authentication.getName();
            System.out.println("用户名: " + username);
            
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            System.out.println("作业标题: " + assignment.getTitle());
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                System.out.println("文件类型错误: " + contentType);
                return ResponseEntity.status(400)
                    .body(Map.of("error", "只能上传PDF文件"));
            }
            
            // 检查文件大小
            if (file.getSize() > 20 * 1024 * 1024) {
                System.out.println("文件过大: " + file.getSize());
                return ResponseEntity.status(400)
                    .body(Map.of("error", "文件大小不能超过20MB"));
            }
            
            Submission submission = submissionService.submitHomework(assignment, user, file, description);
            System.out.println("文件上传成功，提交ID: " + submission.getId());
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件上传失败: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "文件上传失败: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{submissionId}")
    public ResponseEntity<?> deleteSubmission(
        @PathVariable Long submissionId,
        Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        submissionService.deleteSubmission(submissionId, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{submissionId}/feedback")
    public ResponseEntity<Submission> addFeedback(
        @PathVariable Long submissionId,
        @RequestBody Map<String, String> request,
        Authentication authentication
    ) {
        // 检查权限
        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.isAdmin()) {
            return ResponseEntity.status(403).body(null);
        }

        Submission submission = submissionService.addFeedback(submissionId, request.get("feedback"));
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{submissionId}/file")
    public ResponseEntity<Resource> downloadFile(
        @PathVariable Long submissionId,
        Authentication authentication
    ) throws MalformedURLException {
        try {
            System.out.println("开始处理文件下载请求...");
            System.out.println("提交ID: " + submissionId);
            
            // 检查权限
            String username = authentication.getName();
            System.out.println("请求用户: " + username);
            
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    
            // 获取提交记录和文件
            Submission submission = submissionService.getSubmission(submissionId);
            System.out.println("提交记录: " + submission.getId());
            System.out.println("提交用户: " + submission.getSubmittedBy().getUsername());
            System.out.println("提交时间: " + submission.getSubmitTime());
            
            Path file = submissionService.getSubmissionFile(submissionId);
            System.out.println("文件路径: " + file);
            
            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                System.out.println("文件不存在或不可读: " + file);
                throw new RuntimeException("无法读取文件");
            }
            
            System.out.println("文件存在且可读");
            System.out.println("文件大小: " + resource.contentLength() + " 字节");
            
            // 使用原始文件名
            String filename = submission.getOriginalFilename();
            if (filename == null || filename.isEmpty()) {
                filename = file.getFileName().toString();
                System.out.println("使用文件系统文件名: " + filename);
            } else {
                System.out.println("使用原始文件名: " + filename);
            }
            
            // 对文件名进行URL编码，解决中文乱码问题
            String encodedFilename = java.net.URLEncoder.encode(filename, "UTF-8")
                .replaceAll("\\+", "%20");
            
            System.out.println("编码后的文件名: " + encodedFilename);
            
            // 设置Content-Disposition头为单一header，包含标准和RFC 5987格式
            HttpHeaders headers = new HttpHeaders();
            String contentDisposition = "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename;
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            
            // 设置内容类型为二进制流，强制下载
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 禁止缓存
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            System.out.println("文件下载准备完成，返回响应");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
        } catch (Exception e) {
            System.out.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<Submission> getSubmissionById(
        @PathVariable Long submissionId,
        Authentication authentication
    ) {
        try {
            System.out.println("获取提交记录详情，ID: " + submissionId);
            
            // 检查权限
            String username = authentication.getName();
            System.out.println("请求用户: " + username);
            
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取提交记录
            Submission submission = submissionService.getSubmission(submissionId);
            
            // 检查是否是本人的提交或管理员
            if (!submission.getSubmittedBy().getUsername().equals(username) && !user.isAdmin()) {
                System.out.println("无权查看此提交记录");
                return ResponseEntity.status(403).body(null);
            }
            
            System.out.println("提交记录详情:");
            System.out.println("ID: " + submission.getId());
            System.out.println("提交用户: " + submission.getSubmittedBy().getUsername());
            System.out.println("提交时间: " + submission.getSubmitTime());
            System.out.println("原始文件名: " + submission.getOriginalFilename());
            System.out.println("文件路径: " + submission.getFilePath());
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            System.out.println("获取提交记录详情失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}