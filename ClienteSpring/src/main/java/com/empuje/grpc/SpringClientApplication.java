package com.empuje.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {
        "com.empuje.grpc",
        "com.empuje.kafka", // repos, entidades, services (listener)
        "com.empuje.ui" // controller UI
})
@EnableJpaRepositories(basePackages = "com.empuje.kafka.repo")
@EntityScan(basePackages = "com.empuje.kafka.entity")
public class SpringClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringClientApplication.class, args);
    }
}