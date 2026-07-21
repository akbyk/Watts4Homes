package com.akbyk.watts4homes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Watts4HomesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Watts4HomesApplication.class, args);
    }

}
