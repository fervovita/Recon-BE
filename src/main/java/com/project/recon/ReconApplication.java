package com.project.recon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ReconApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReconApplication.class, args);
    }

}
