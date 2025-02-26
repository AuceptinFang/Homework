-- 清理现有数据（如果存在）
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE submissions;
-- TRUNCATE TABLE assignments;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- 创建管理员账号 (密码: admin123)
-- 注意：密码使用BCrypt加密，这是实际的admin123加密后的值
INSERT INTO users (username, password, is_admin) 
VALUES ('admin', '$2a$10$QFjYSS51FWhyyRTdMjbuMOjAl23mj7nWCOKjGEN0daMuGILdDD5vq', true);

-- 创建示例作业
INSERT INTO assignments (title, description, deadline, create_time, created_by, allow_submit, file_format) 
VALUES 
('第一次作业', '请提交PDF格式的实验报告', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), 1, true, 'pdf'),
('第二次作业', '请提交PDF格式的课程设计文档', DATE_ADD(NOW(), INTERVAL 14 DAY), NOW(), 1, true, 'pdf');

-- 插入普通用户（初始密码为NULL，需要注册设置）
INSERT INTO users (username, password, is_admin)
VALUES 
('段鹏鹏', NULL, false),
('方震', NULL, false),
('高天全', NULL, false),
('高子涵', NULL, false),
('龚皓', NULL, false),
('霍雨婷', NULL, false),
('贾奇燠', NULL, false),
('贾一丁', NULL, false),
('金闿翎', NULL, false),
('靳宇晨', NULL, false),
('李希', NULL, false),
('刘森源', NULL, false),
('刘文晶', NULL, false),
('吕彦儒', NULL, false),
('吕泽熙', NULL, false),
('苏宗佑', NULL, false),
('汤锦健', NULL, false),
('王奕涵', NULL, false),
('王悦琳', NULL, false),
('王志成', NULL, false),
('王墨', NULL, false),
('王泽君', NULL, false),
('夏正鑫', NULL, false),
('肖俊波', NULL, false),
('杨海屹', NULL, false),
('杨孟涵', NULL, false),
('张成宇', NULL, false),
('张峻菘', NULL, false),
('张钟文', NULL, false),
('赵萌', NULL, false)
ON DUPLICATE KEY UPDATE username = username;