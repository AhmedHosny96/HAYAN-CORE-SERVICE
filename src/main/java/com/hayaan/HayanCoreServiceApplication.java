package com.hayaan;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class HayanCoreServiceApplication {


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


    public static void main(String[] args) {

        SpringApplication.run(HayanCoreServiceApplication.class, args);

        String totalMinutesString = "260";
        int totalMinutes = Integer.parseInt(totalMinutesString);
        Duration duration = Duration.ofMinutes(totalMinutes);
        System.out.println("Duration: " + duration);
    }


}
