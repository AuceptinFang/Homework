package com.example.homeworksystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class HomeworkSystemApplication {
    private static final Logger logger = LoggerFactory.getLogger(HomeworkSystemApplication.class);

    public static void main(String[] args) {
        try {
            ApplicationContext context = SpringApplication.run(HomeworkSystemApplication.class, args);
            logger.info("应用程序启动成功");
            
            // 打印活动的配置文件
            String[] activeProfiles = context.getEnvironment().getActiveProfiles();
            logger.info("活动的配置文件: {}", String.join(", ", activeProfiles));
            
            // 打印数据源信息
            String dbUrl = context.getEnvironment().getProperty("spring.datasource.url");
            logger.info("数据库URL: {}", dbUrl);
        } catch (Exception e) {
            logger.error("应用程序启动失败", e);
            System.exit(1);
        }
    }
} 