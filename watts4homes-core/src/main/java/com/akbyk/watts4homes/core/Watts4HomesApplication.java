package com.akbyk.watts4homes.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Watts4HomesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Watts4HomesApplication.class, args);
    }

}
