package com.empuje.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "com.empuje")
@EnableJpaRepositories(basePackages = "com.empuje")
@EntityScan(basePackages = "com.empuje")
public class SpringClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringClientApplication.class, args);
        System.out.println("\nEjecución lista. Abra su navegador en:\nhttp://localhost:8080\n=========================================================\n");
    }
}
