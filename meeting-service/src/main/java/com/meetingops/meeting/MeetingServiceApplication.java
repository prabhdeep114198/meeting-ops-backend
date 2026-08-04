package com.meetingops.meeting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.meetingops")
@EntityScan(basePackages = "com.meetingops.infrastructure.jpa.entity")
@EnableJpaRepositories(basePackages = "com.meetingops.infrastructure.jpa.repository")
public class MeetingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingServiceApplication.class, args);
    }
}
