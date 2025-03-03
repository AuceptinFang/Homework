package com.example.homeworksystem.config;

import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataInitializer {

    /**
     * 初始化学生数据
     */
    @Bean
    @Order(2) // 在管理员账户创建后执行
    public CommandLineRunner initStudentData(UserService userService) {
        return args -> {
            System.out.println("开始初始化学生数据...");
            
            // 学生信息列表
            List<Map<String, String>> studentList = new ArrayList<>();
            
            // 添加学生信息
            addStudent(studentList, "2024211349", "贾一丁");
            addStudent(studentList, "2024211350", "肖俊波");
            addStudent(studentList, "2024211351", "刘森源");
            addStudent(studentList, "2024211352", "杨孟涵");
            addStudent(studentList, "2024211353", "靳宇晨");
            addStudent(studentList, "2024211354", "王志成");
            addStudent(studentList, "2024211355", "方震");
            addStudent(studentList, "2024211356", "杨海屹");
            addStudent(studentList, "2024211357", "汤锦健");
            addStudent(studentList, "2024211358", "王泽军");
            addStudent(studentList, "2024211359", "张成宇");
            addStudent(studentList, "2024211360", "王墨");
            addStudent(studentList, "2024211361", "张钟文");
            addStudent(studentList, "2024211362", "夏正鑫");
            addStudent(studentList, "2024211363", "苏宗佑");
            addStudent(studentList, "2024211364", "龚皓");
            addStudent(studentList, "2024211365", "王奕涵");
            addStudent(studentList, "2024211366", "张峻菘");
            addStudent(studentList, "2024211367", "贾奇燠");
            addStudent(studentList, "2024211368", "段鹏鹏");
            addStudent(studentList, "2024211369", "高天全");
            addStudent(studentList, "2024211370", "吕彦儒");
            addStudent(studentList, "2024211371", "赵萌");
            addStudent(studentList, "2024211372", "霍雨婷");
            addStudent(studentList, "2024211373", "王悦琳");
            addStudent(studentList, "2024211374", "刘文晶");
            addStudent(studentList, "2024211375", "吕泽西");
            addStudent(studentList, "2024211376", "李希");
            addStudent(studentList, "2024211377", "高子涵");
            addStudent(studentList, "2024211378", "金闿翎");
            
            int successCount = 0;
            int existCount = 0;
            
            // 导入学生信息
            for (Map<String, String> studentInfo : studentList) {
                String username = studentInfo.get("username");
                String realName = studentInfo.get("realName");
                String studentId = studentInfo.get("studentId");
                
                try {
                    // 检查用户是否已存在
                    if (userService.findByUsername(username).isPresent()) {
                        User existingUser = userService.findByUsername(username).get();
                        // 更新用户信息
                        existingUser.setRealName(realName);
                        existingUser.setStudentId(studentId);
                        userService.saveUser(existingUser);
                        existCount++;
                        System.out.println("更新学生信息: " + username + " (" + realName + ", " + studentId + ")");
                    } else {
                        // 创建新用户
                        User newUser = userService.createUser(username, null);
                        newUser.setRealName(realName);
                        newUser.setStudentId(studentId);
                        userService.saveUser(newUser);
                        successCount++;
                        System.out.println("创建学生: " + username + " (" + realName + ", " + studentId + ")");
                    }
                } catch (Exception e) {
                    System.err.println("导入学生 " + username + " 失败: " + e.getMessage());
                }
            }
            
            System.out.println("学生数据初始化完成: 新增 " + successCount + " 名学生, 更新 " + existCount + " 名学生");
        };
    }
    
    /**
     * 添加学生信息到列表
     */
    private void addStudent(List<Map<String, String>> list, String studentId, String realName) {
        Map<String, String> student = new HashMap<>();
        student.put("username", realName); // 用真实姓名作为用户名
        student.put("realName", realName);
        student.put("studentId", studentId);
        list.add(student);
    }
} 