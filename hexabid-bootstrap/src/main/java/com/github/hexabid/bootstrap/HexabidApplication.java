package com.github.hexabid.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EntityScan(basePackages = "com.github.hexabid.adapter.out.db")
@EnableJpaRepositories(basePackages = "com.github.hexabid.adapter.out.db")
@SpringBootApplication(scanBasePackages = "com.github.hexabid")
public class HexabidApplication {

    public static void main(String[] args) {
        SpringApplication.run(HexabidApplication.class, args);
    }
}
