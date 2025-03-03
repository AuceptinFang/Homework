package com.example.homeworksystem.service;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.Submission;
import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.repository.SubmissionRepository;
import com.example.homeworksystem.util.PdfUtil;
import com.itextpdf.text.DocumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.Collections;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final Path uploadDir;

    @Value("${file.upload-dir}")
    private String configuredUploadDir;

    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
        
        // 初始化为默认值，将在afterPropertiesSet中被正确配置的值覆盖
        this.uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath();
        try {
            Files.createDirectories(this.uploadDir);
            System.out.println("临时上传目录创建成功：" + this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建临时上传目录: " + this.uploadDir, e);
        }
    }
    
    @PostConstruct
    public void afterPropertiesSet() {
        // 使用配置文件中的路径
        Path configuredPath = Paths.get(configuredUploadDir).toAbsolutePath();
        try {
            Files.createDirectories(configuredPath);
            System.out.println("配置的上传目录创建成功：" + configuredPath);
            // 这里不能直接修改uploadDir，因为它是final的，但我们可以确保它已经被正确初始化
            if (!configuredPath.equals(this.uploadDir)) {
                System.out.println("警告：配置的上传目录与默认目录不同，请确保所有文件都存储在配置的目录中：" + configuredPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建配置的上传目录: " + configuredPath, e);
        }
    }

    public List<Submission> getSubmissionsByAssignment(Assignment assignment) {
        if (assignment == null) {
            System.out.println("警告: 传入的作业为null");
            return Collections.emptyList();
        }
        
        List<Submission> submissions = submissionRepository.findByAssignmentOrderBySubmitTimeDesc(assignment);
        return submissions;
    }

    public List<Submission> getSubmissionsByUser(User user) {
        if (user == null) {
            // 管理员查看所有提交记录
            return submissionRepository.findAll();
        }
        // 普通用户只能查看自己的提交记录
        return submissionRepository.findBySubmittedByOrderBySubmitTimeDesc(user);
    }

    @Transactional
    public Submission submitHomework(Assignment assignment, User user, MultipartFile file, String description) throws IOException {
        System.out.println("SubmissionService.submitHomework - 开始处理...");
        
        // 检查作业是否允许提交
        if (!assignment.isAllowSubmit()) {
            System.out.println("作业不允许提交: " + assignment.getTitle());
            throw new RuntimeException("该作业当前不允许提交");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        System.out.println("文件类型: " + contentType);
        if (contentType == null || !contentType.equals("application/pdf")) {
            System.out.println("文件类型错误: " + contentType);
            throw new RuntimeException("只能提交PDF文件");
        }

        // 检查文件大小（限制为20MB）
        System.out.println("文件大小: " + file.getSize());
        if (file.getSize() > 20 * 1024 * 1024) {
            System.out.println("文件过大: " + file.getSize());
            throw new RuntimeException("文件大小不能超过20MB");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        System.out.println("原始文件名: " + originalFilename);
        
        // 获取文件扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 使用学号+姓名作为文件名
        String uniqueFilename = generateFilePrefix(user) + fileExtension;
        System.out.println("使用的文件名: " + uniqueFilename);

        // 为作业创建一个子目录，文件夹名基于作业标题（经过清洗）
        String assignmentFolderName = sanitizeTitle(assignment.getTitle());
        Path assignmentFolder = this.uploadDir.resolve(assignmentFolderName);
        Files.createDirectories(assignmentFolder);
        System.out.println("为作业 '" + assignment.getTitle() + "' 创建文件夹: " + assignmentFolder);

        // 存储文件到对应的子目录
        Path filePath = assignmentFolder.resolve(uniqueFilename);
        System.out.println("保存文件到: " + filePath);
        
        try {
            // 确保上传目录存在
            Files.createDirectories(assignmentFolder);
            
            // 检查目录是否可写
            if (!Files.isWritable(assignmentFolder)) {
                System.out.println("上传目录不可写: " + assignmentFolder);
                throw new RuntimeException("上传目录不可写: " + assignmentFolder);
            }
            
            // 保存文件，如果已存在则覆盖
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件保存成功: " + filePath);
        } catch (IOException e) {
            System.out.println("保存文件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存文件失败: " + e.getMessage(), e);
        }

        // 检查是否已有提交记录，如果有则更新，否则创建新记录
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndSubmittedBy(assignment, user);
        Submission submission;
        
        if (existingSubmission.isPresent()) {
            // 更新现有记录
            submission = existingSubmission.get();
            submission.setSubmitTime(LocalDateTime.now());
            submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
            submission.setOriginalFilename(originalFilename);
            submission.setDescription(description);
            System.out.println("更新现有提交记录，ID: " + submission.getId());
        } else {
            // 创建新的提交记录
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setSubmittedBy(user);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus("submitted");
            submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
            submission.setOriginalFilename(originalFilename);
            submission.setDescription(description);
            System.out.println("创建新的提交记录");
        }

        Submission savedSubmission = submissionRepository.save(submission);
        System.out.println("提交记录保存成功，ID: " + savedSubmission.getId());
        
        return savedSubmission;
    }

    @Transactional
    public void deleteSubmission(Long submissionId, User user) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("提交记录不存在"));

        // 检查是否是本人的提交
        if (!submission.getSubmittedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new RuntimeException("无权删除此提交");
        }

        try {
            // 尝试删除文件，但即使失败也继续删除记录
            deleteSubmissionFile(submission);
        } catch (Exception e) {
            // 记录错误但不抛出异常
            System.err.println("删除文件失败，但将继续删除记录: " + e.getMessage());
        }

        // 删除记录
        submissionRepository.delete(submission);
    }

    private void deleteSubmissionFile(Submission submission) {
        try {
            Path filePath = this.uploadDir.resolve(submission.getFilePath());
            System.out.println("准备删除文件: " + filePath);
            System.out.println("原始文件名: " + submission.getOriginalFilename());
            System.out.println("提交ID: " + submission.getId());
            System.out.println("提交用户: " + submission.getSubmittedBy().getUsername());
            
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                System.out.println("文件删除成功: " + filePath);
            } else {
                System.out.println("文件不存在或无法删除: " + filePath);
            }
        } catch (IOException e) {
            System.out.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，让调用者决定如何处理
        }
    }

    @Transactional
    public Submission addFeedback(Long submissionId, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("提交记录不存在"));
        submission.setFeedback(feedback);
        return submissionRepository.save(submission);
    }

    public Submission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
            .orElseThrow(() -> new RuntimeException("提交记录不存在"));
    }

    public Path getSubmissionFile(Long submissionId) {
        Submission submission = getSubmission(submissionId);
        return this.uploadDir.resolve(submission.getFilePath());
    }

    private String sanitizeTitle(String title) {
        // 修改标题清洗逻辑，允许所有Unicode的字母和数字
        return title.replaceAll("[^\\p{L}\\p{N}]", "_");
    }

    /**
     * 生成文件名前缀，格式为：学号+姓名
     * 如果学号或姓名为空，则使用用户名
     */
    private String generateFilePrefix(User user) {
        if (user.getStudentId() != null && !user.getStudentId().isEmpty() 
                && user.getRealName() != null && !user.getRealName().isEmpty()) {
            return user.getStudentId() + "_" + user.getRealName();
        }
        return user.getUsername();
    }

    /**
     * 处理多张图片上传并合并为PDF
     *
     * @param assignment 作业
     * @param user 用户
     * @param imageFiles 图片文件列表
     * @param description 描述
     * @return 提交记录
     * @throws IOException 如果文件操作失败
     * @throws DocumentException 如果PDF文档操作失败
     */
    @Transactional
    public Submission submitImages(Assignment assignment, User user, List<MultipartFile> imageFiles, String description) throws IOException, DocumentException {
        System.out.println("SubmissionService.submitImages - 开始处理...");
        
        // 检查作业是否允许提交
        if (!assignment.isAllowSubmit()) {
            System.out.println("作业不允许提交: " + assignment.getTitle());
            throw new RuntimeException("该作业当前不允许提交");
        }
        
        // 为作业创建一个子目录，文件夹名基于作业标题（经过清洗）
        String assignmentFolderName = sanitizeTitle(assignment.getTitle());
        Path assignmentFolder = this.uploadDir.resolve(assignmentFolderName);
        Files.createDirectories(assignmentFolder);
        System.out.println("为作业 '" + assignment.getTitle() + "' 创建文件夹: " + assignmentFolder);
        
        // 创建PDF文件名（使用学号+姓名）
        String pdfFilename = generateFilePrefix(user) + ".pdf";
        Path pdfPath = assignmentFolder.resolve(pdfFilename);
        System.out.println("PDF文件路径: " + pdfPath);
        
        // 将图片合并为PDF
        PdfUtil.imagesToPdf(imageFiles, pdfPath);
        System.out.println("图片已合并为PDF: " + pdfPath);
        
        // 检查是否已有提交记录，如果有则更新，否则创建新记录
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndSubmittedBy(assignment, user);
        Submission submission;
        
        // 保存原始文件名（多个图片用逗号分隔）
        StringBuilder originalFilenames = new StringBuilder();
        for (int i = 0; i < imageFiles.size(); i++) {
            if (i > 0) originalFilenames.append(", ");
            originalFilenames.append(imageFiles.get(i).getOriginalFilename());
        }
        String originalFilenamesStr = originalFilenames.toString();
        
        if (existingSubmission.isPresent()) {
            // 更新现有记录
            submission = existingSubmission.get();
            submission.setSubmitTime(LocalDateTime.now());
            submission.setFilePath(assignmentFolderName + "/" + pdfFilename);
            submission.setOriginalFilename(originalFilenamesStr);
            submission.setDescription(description);
            System.out.println("更新现有提交记录，ID: " + submission.getId());
        } else {
            // 创建新的提交记录
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setSubmittedBy(user);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus("submitted");
            submission.setFilePath(assignmentFolderName + "/" + pdfFilename);
            submission.setOriginalFilename(originalFilenamesStr);
            submission.setDescription(description);
            System.out.println("创建新的提交记录");
        }
        
        Submission savedSubmission = submissionRepository.save(submission);
        System.out.println("提交记录保存成功，ID: " + savedSubmission.getId());
        
        return savedSubmission;
    }

    /**
     * 处理多张图片上传但不合并为PDF（用于非PDF限制的作业）
     *
     * @param assignment 作业
     * @param user 用户
     * @param imageFiles 图片文件列表
     * @param description 描述
     * @return 提交记录
     * @throws IOException 如果文件操作失败
     */
    @Transactional
    public Submission submitRawImages(Assignment assignment, User user, List<MultipartFile> imageFiles, String description) throws IOException {
        System.out.println("SubmissionService.submitRawImages - 开始处理...");
        
        // 检查作业是否允许提交
        if (!assignment.isAllowSubmit()) {
            System.out.println("作业不允许提交: " + assignment.getTitle());
            throw new RuntimeException("该作业当前不允许提交");
        }
        
        // 为作业创建一个子目录，文件夹名基于作业标题（经过清洗）
        String assignmentFolderName = sanitizeTitle(assignment.getTitle());
        Path assignmentFolder = this.uploadDir.resolve(assignmentFolderName);
        Files.createDirectories(assignmentFolder);
        System.out.println("为作业 '" + assignment.getTitle() + "' 创建文件夹: " + assignmentFolder);
        
        // 检查是否已有提交记录
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndSubmittedBy(assignment, user);
        
        // 如果只有一张图片，直接保存
        if (imageFiles.size() == 1) {
            MultipartFile imageFile = imageFiles.get(0);
            String originalFilename = imageFile.getOriginalFilename();
            
            // 获取文件扩展名
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 使用学号+姓名作为文件名
            String uniqueFilename = generateFilePrefix(user) + fileExtension;
            Path filePath = assignmentFolder.resolve(uniqueFilename);
            
            try {
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("图片已保存: " + filePath);
                
                Submission submission;
                
                if (existingSubmission.isPresent()) {
                    // 更新现有记录
                    submission = existingSubmission.get();
                    submission.setSubmitTime(LocalDateTime.now());
                    submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
                    submission.setOriginalFilename(originalFilename);
                    submission.setDescription(description);
                    System.out.println("更新现有提交记录，ID: " + submission.getId());
                } else {
                    // 创建新的提交记录
                    submission = new Submission();
                    submission.setAssignment(assignment);
                    submission.setSubmittedBy(user);
                    submission.setSubmitTime(LocalDateTime.now());
                    submission.setStatus("submitted");
                    submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
                    submission.setOriginalFilename(originalFilename);
                    submission.setDescription(description);
                    System.out.println("创建新的提交记录");
                }
                
                Submission savedSubmission = submissionRepository.save(submission);
                System.out.println("提交记录保存成功，ID: " + savedSubmission.getId());
                
                return savedSubmission;
            } catch (IOException e) {
                System.out.println("保存图片失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
            }
        } 
        // 如果有多张图片，创建一个压缩文件
        else {
            // 使用学号+姓名作为文件名
            String zipFilename = generateFilePrefix(user) + ".zip";
            Path zipPath = assignmentFolder.resolve(zipFilename);
            
            try {
                // 创建临时目录存放图片
                Path tempDir = Files.createTempDirectory("images_");
                
                // 保存所有图片到临时目录
                for (int i = 0; i < imageFiles.size(); i++) {
                    MultipartFile imageFile = imageFiles.get(i);
                    String originalFilename = imageFile.getOriginalFilename();
                    if (originalFilename == null) {
                        originalFilename = "image_" + i + ".jpg";
                    }
                    
                    Path tempFilePath = tempDir.resolve(originalFilename);
                    Files.copy(imageFile.getInputStream(), tempFilePath);
                }
                
                // 创建ZIP文件
                try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
                     ZipOutputStream zos = new ZipOutputStream(fos)) {
                    
                    Files.walk(tempDir)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(tempDir.relativize(path).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException("创建ZIP文件失败: " + e.getMessage(), e);
                            }
                        });
                }
                
                // 删除临时目录
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.out.println("删除临时文件失败: " + path);
                        }
                    });
                
                // 保存原始文件名（多个图片用逗号分隔）
                StringBuilder originalFilenames = new StringBuilder();
                for (int i = 0; i < imageFiles.size(); i++) {
                    if (i > 0) originalFilenames.append(", ");
                    originalFilenames.append(imageFiles.get(i).getOriginalFilename());
                }
                String originalFilenamesStr = originalFilenames.toString();
                
                Submission submission;
                
                if (existingSubmission.isPresent()) {
                    // 更新现有记录
                    submission = existingSubmission.get();
                    submission.setSubmitTime(LocalDateTime.now());
                    submission.setFilePath(assignmentFolderName + "/" + zipFilename);
                    submission.setOriginalFilename(originalFilenamesStr);
                    submission.setDescription(description);
                    System.out.println("更新现有提交记录，ID: " + submission.getId());
                } else {
                    // 创建新的提交记录
                    submission = new Submission();
                    submission.setAssignment(assignment);
                    submission.setSubmittedBy(user);
                    submission.setSubmitTime(LocalDateTime.now());
                    submission.setStatus("submitted");
                    submission.setFilePath(assignmentFolderName + "/" + zipFilename);
                    submission.setOriginalFilename(originalFilenamesStr);
                    submission.setDescription(description);
                    System.out.println("创建新的提交记录");
                }
                
                Submission savedSubmission = submissionRepository.save(submission);
                System.out.println("提交记录保存成功，ID: " + savedSubmission.getId());
                
                return savedSubmission;
            } catch (IOException e) {
                System.out.println("创建ZIP文件失败: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("创建ZIP文件失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 处理其他格式文件上传（用于非PDF限制的作业）
     *
     * @param assignment 作业
     * @param user 用户
     * @param file 文件
     * @param description 描述
     * @return 提交记录
     * @throws IOException 如果文件操作失败
     */
    @Transactional
    public Submission submitOtherFile(Assignment assignment, User user, MultipartFile file, String description) throws IOException {
        System.out.println("SubmissionService.submitOtherFile - 开始处理...");
        
        // 检查作业是否允许提交
        if (!assignment.isAllowSubmit()) {
            System.out.println("作业不允许提交: " + assignment.getTitle());
            throw new RuntimeException("该作业当前不允许提交");
        }
        
        // 检查作业是否允许非PDF格式
        if ("pdf".equals(assignment.getFileFormat())) {
            System.out.println("作业要求PDF格式: " + assignment.getTitle());
            throw new RuntimeException("该作业只允许提交PDF格式文件");
        }
        
        // 检查文件大小（限制为20MB）
        System.out.println("文件大小: " + file.getSize());
        if (file.getSize() > 20 * 1024 * 1024) {
            System.out.println("文件过大: " + file.getSize());
            throw new RuntimeException("文件大小不能超过20MB");
        }
        
        // 为作业创建一个子目录，文件夹名基于作业标题（经过清洗）
        String assignmentFolderName = sanitizeTitle(assignment.getTitle());
        Path assignmentFolder = this.uploadDir.resolve(assignmentFolderName);
        Files.createDirectories(assignmentFolder);
        System.out.println("为作业 '" + assignment.getTitle() + "' 创建文件夹: " + assignmentFolder);
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        System.out.println("原始文件名: " + originalFilename);
        
        // 获取文件扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // 使用学号+姓名作为文件名
        String uniqueFilename = generateFilePrefix(user) + fileExtension;
        System.out.println("使用的文件名: " + uniqueFilename);
        
        // 存储文件到对应的子目录
        Path filePath = assignmentFolder.resolve(uniqueFilename);
        System.out.println("保存文件到: " + filePath);
        
        try {
            // 确保上传目录存在
            Files.createDirectories(assignmentFolder);
            
            // 检查目录是否可写
            if (!Files.isWritable(assignmentFolder)) {
                System.out.println("上传目录不可写: " + assignmentFolder);
                throw new RuntimeException("上传目录不可写: " + assignmentFolder);
            }
            
            // 保存文件，如果已存在则覆盖
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件保存成功: " + filePath);
        } catch (IOException e) {
            System.out.println("保存文件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("保存文件失败: " + e.getMessage(), e);
        }
        
        // 检查是否已有提交记录，如果有则更新，否则创建新记录
        Optional<Submission> existingSubmission = submissionRepository.findByAssignmentAndSubmittedBy(assignment, user);
        Submission submission;
        
        if (existingSubmission.isPresent()) {
            // 更新现有记录
            submission = existingSubmission.get();
            submission.setSubmitTime(LocalDateTime.now());
            submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
            submission.setOriginalFilename(originalFilename);
            submission.setDescription(description);
            System.out.println("更新现有提交记录，ID: " + submission.getId());
        } else {
            // 创建新的提交记录
            submission = new Submission();
            submission.setAssignment(assignment);
            submission.setSubmittedBy(user);
            submission.setSubmitTime(LocalDateTime.now());
            submission.setStatus("submitted");
            submission.setFilePath(assignmentFolderName + "/" + uniqueFilename);
            submission.setOriginalFilename(originalFilename);
            submission.setDescription(description);
            System.out.println("创建新的提交记录");
        }
        
        Submission savedSubmission = submissionRepository.save(submission);
        System.out.println("提交记录保存成功，ID: " + savedSubmission.getId());
        
        return savedSubmission;
    }
}