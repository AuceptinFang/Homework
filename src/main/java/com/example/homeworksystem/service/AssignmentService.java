package com.example.homeworksystem.service;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.repository.AssignmentRepository;
import com.example.homeworksystem.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<Assignment> getAvailableAssignments() {
        return assignmentRepository.findByAllowSubmitTrueOrderByCreateTimeDesc();
    }

    @Transactional
    public Assignment createAssignment(String title, String description, LocalDateTime deadline, User createdBy, String fileFormat) {
        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDeadline(deadline);
        assignment.setCreateTime(LocalDateTime.now());
        assignment.setCreatedBy(createdBy);
        assignment.setAllowSubmit(true);
        assignment.setFileFormat(fileFormat);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment updateSubmissionControl(Long id, boolean allowSubmit) {
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("作业不存在"));
        assignment.setAllowSubmit(allowSubmit);
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        // 先获取作业
        Assignment assignment = assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("作业不存在"));
        
        // 先删除与该作业相关的所有提交记录，以解决外键约束问题
        submissionRepository.deleteByAssignment(assignment);
        
        // 然后删除作业
        assignmentRepository.deleteById(id);
    }

    public Assignment getAssignment(Long id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("作业不存在"));
    }
} 