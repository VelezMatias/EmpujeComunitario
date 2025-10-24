package com.empuje.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = {
        "com.empuje.grpc",
        "com.empuje.kafka",
        "com.empuje.ui",
        "com.empuje.graphql",
        "com.empuje.repo",
        "com.empuje.controller",
        "com.empuje.service"
})
@EnableJpaRepositories(basePackages = "com.empuje.kafka.repo")
@EntityScan(basePackages = "com.empuje.kafka.entity")
public class SpringClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringClientApplication.class, args);
        System.out.println("\nEjecución lista. Abra su navegador en:\nhttp://localhost:8080\n=========================================================\n");
    }
}
