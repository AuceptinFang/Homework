package com.example.homeworksystem;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTest {
    
    @Test
    public void generateEncodedPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("admin123");
        System.out.println("Encoded password for admin123: " + encodedPassword);
        
        // 验证密码是否正确
        boolean matches = encoder.matches("admin123", encodedPassword);
        System.out.println("Password matches: " + matches);
    }
} 