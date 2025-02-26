package com.example.homeworksystem.repository;

import com.example.homeworksystem.entity.Assignment;
import com.example.homeworksystem.entity.Submission;
import com.example.homeworksystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentOrderBySubmitTimeDesc(Assignment assignment);
    List<Submission> findBySubmittedByOrderBySubmitTimeDesc(User user);
    Optional<Submission> findByAssignmentAndSubmittedBy(Assignment assignment, User user);
    
    @Query("SELECT s FROM Submission s ORDER BY s.submitTime DESC")
    List<Submission> findAll();
} 