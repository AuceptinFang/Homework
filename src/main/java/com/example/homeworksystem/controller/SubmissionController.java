package com.example.homeworksystem.controller;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.Submission;
import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.AssignmentService;
import com.example.homeworksystem.service.SubmissionService;
import com.example.homeworksystem.service.UserService;
import com.example.homeworksystem.util.PdfUtil;
import com.itextpdf.text.DocumentException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin
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

            // 获取所有学生用户
            List<User> allUsers = userService.getAllStudents();
            
            // 获取该作业的所有提交记录
            List<Submission> submissions = submissionService.getSubmissionsByAssignment(assignment);
            
            // 创建一个Map，用于快速查找用户的提交记录
            Map<String, Submission> submissionMap = new HashMap<>();
            for (Submission submission : submissions) {
                submissionMap.put(submission.getSubmittedBy().getUsername(), submission);
            }
            
            // 创建结果列表
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 为每个用户创建一个结果项
            for (User student : allUsers) {
                Map<String, Object> item = new HashMap<>();
                item.put("username", student.getUsername());
                
                // 检查用户是否有提交记录
                if (submissionMap.containsKey(student.getUsername())) {
                    Submission submission = submissionMap.get(student.getUsername());
                    item.put("submitted", true);
                    item.put("submitTime", submission.getSubmitTime());
                    item.put("status", submission.getStatus());
                    item.put("filePath", submission.getFilePath());
                    item.put("originalFilename", submission.getOriginalFilename());
                    item.put("description", submission.getDescription());
                    item.put("feedback", submission.getFeedback());
                    item.put("submissionId", submission.getId());
                } else {
                    item.put("submitted", false);
                    item.put("status", "未提交");
                }
                
                result.add(item);
            }
            
            return ResponseEntity.ok(result);
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

    /**
     * 上传多张图片并自动合并为PDF
     */
    @PostMapping("/assignment/{assignmentId}/images")
    public ResponseEntity<?> submitImages(
        @PathVariable Long assignmentId,
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(required = false) String description,
        Authentication authentication
    ) {
        try {
            System.out.println("开始处理多图片上传请求（转PDF）...");
            System.out.println("作业ID: " + assignmentId);
            System.out.println("上传文件数量: " + files.length);
            
            String username = authentication.getName();
            System.out.println("用户名: " + username);
            
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            System.out.println("作业标题: " + assignment.getTitle());
            
            // 检查是否有文件上传
            if (files.length == 0) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "请选择至少一张图片"));
            }
            
            // 过滤出图片文件
            List<MultipartFile> imageFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    imageFiles.add(file);
                    System.out.println("添加图片: " + file.getOriginalFilename() + ", 类型: " + contentType);
                } else {
                    System.out.println("跳过非图片文件: " + file.getOriginalFilename() + ", 类型: " + contentType);
                }
            }
            
            if (imageFiles.isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "没有有效的图片文件"));
            }
            
            // 调用服务处理图片上传并合并为PDF
            Submission submission = submissionService.submitImages(assignment, user, imageFiles, description);
            System.out.println("图片上传并合并为PDF成功，提交ID: " + submission.getId());
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("图片上传失败: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "图片上传失败: " + e.getMessage()));
        }
    }

    /**
     * 上传多张图片但不合并为PDF（用于非PDF限制的作业）
     */
    @PostMapping("/assignment/{assignmentId}/raw-images")
    public ResponseEntity<?> submitRawImages(
        @PathVariable Long assignmentId,
        @RequestParam("files") MultipartFile[] files,
        @RequestParam(required = false) String description,
        Authentication authentication
    ) {
        try {
            System.out.println("开始处理多图片上传请求（不转PDF）...");
            System.out.println("作业ID: " + assignmentId);
            System.out.println("上传文件数量: " + files.length);
            
            String username = authentication.getName();
            System.out.println("用户名: " + username);
            
            User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Assignment assignment = assignmentService.getAssignment(assignmentId);
            System.out.println("作业标题: " + assignment.getTitle());
            
            // 检查作业是否允许非PDF格式
            if ("pdf".equals(assignment.getFileFormat())) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "该作业只允许提交PDF格式文件"));
            }
            
            // 检查是否有文件上传
            if (files.length == 0) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "请选择至少一张图片"));
            }
            
            // 过滤出图片文件
            List<MultipartFile> imageFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    imageFiles.add(file);
                    System.out.println("添加图片: " + file.getOriginalFilename() + ", 类型: " + contentType);
                } else {
                    System.out.println("跳过非图片文件: " + file.getOriginalFilename() + ", 类型: " + contentType);
                }
            }
            
            if (imageFiles.isEmpty()) {
                return ResponseEntity.status(400)
                    .body(Map.of("error", "没有有效的图片文件"));
            }
            
            // 调用服务处理图片上传（不合并为PDF）
            Submission submission = submissionService.submitRawImages(assignment, user, imageFiles, description);
            System.out.println("图片上传成功，提交ID: " + submission.getId());
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("图片上传失败: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "图片上传失败: " + e.getMessage()));
        }
    }
    
    /**
     * 上传其他格式文件（用于非PDF限制的作业）
     */
    @PostMapping("/assignment/{assignmentId}/other")
    public ResponseEntity<?> submitOtherFile(
        @PathVariable Long assignmentId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String description,
        Authentication authentication
    ) {
        try {
            System.out.println("开始处理其他格式文件上传请求...");
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
            
            // 检查作业是否允许非PDF格式
            if ("pdf".equals(assignment.getFileFormat())) {
                System.out.println("作业要求PDF格式: " + assignment.getTitle());
                return ResponseEntity.status(400)
                    .body(Map.of("error", "该作业只允许提交PDF格式文件"));
            }
            
            // 检查文件大小
            if (file.getSize() > 20 * 1024 * 1024) {
                System.out.println("文件过大: " + file.getSize());
                return ResponseEntity.status(400)
                    .body(Map.of("error", "文件大小不能超过20MB"));
            }
            
            // 调用服务处理其他格式文件上传
            Submission submission = submissionService.submitOtherFile(assignment, user, file, description);
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