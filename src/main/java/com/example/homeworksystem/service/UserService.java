package com.example.homeworksystem.service;

import com.example.homeworksystem.entity.User;
import com.example.homeworksystem.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // 检查是否已存在管理员账户
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            // 创建管理员账户，使用加密密码
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // 使用加密存储
            admin.setAdmin(true);
            userRepository.save(admin);
        } else {
            // 检查现有管理员账户的密码是否需要更新为加密形式
            User admin = adminOpt.get();
            if (!admin.getPassword().startsWith("$2a$")) { // 检查是否是BCrypt加密的密码
                admin.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
            }
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER"))
        );
    }

    @Transactional
    public User createUser(String username, String password) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // 如果用户存在但密码为空，说明是未注册用户，允许设置密码
            if (user.getPassword() == null && password != null) {
                user.setPassword(passwordEncoder.encode(password));
                return userRepository.save(user);
            }
            if (password != null) {
                throw new RuntimeException("用户名已注册");
            }
            return user;
        }

        User user = new User();
        user.setUsername(username);
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setAdmin(false);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 根据ID查找用户
     * @param id 用户ID
     * @return 用户对象（可选）
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            throw new RuntimeException("该用户尚未注册，请先注册");
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户名不存在"));

        if (user.getPassword() == null) {
            throw new RuntimeException("该用户尚未注册，请先注册");
        }

        if (!validatePassword(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }

    /**
     * 保存用户信息
     * @param user 用户对象
     * @return 保存后的用户对象
     */
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 获取所有学生用户（非管理员用户）
     * @return 学生用户列表
     */
    public List<User> getAllStudents() {
        return userRepository.findByIsAdminFalse();
    }
}