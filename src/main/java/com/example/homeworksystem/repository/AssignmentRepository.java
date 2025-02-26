package com.example.homeworksystem.repository;

import com.example.homeworksystem.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByOrderByCreateTimeDesc();
    List<Assignment> findByAllowSubmitTrueOrderByCreateTimeDesc();
} 