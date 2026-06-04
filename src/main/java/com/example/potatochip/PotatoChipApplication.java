package com.example.potatochip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PotatoChipApplication {

    public static void main(String[] args) {
        SpringApplication.run(PotatoChipApplication.class, args);
    }

}
