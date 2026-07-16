package com.ljl.studyexammanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class StudyExamManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyExamManagementSystemApplication.class, args);
    }

}
