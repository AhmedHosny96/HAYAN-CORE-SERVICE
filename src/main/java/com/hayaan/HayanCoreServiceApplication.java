package com.hayaan;

import com.hayaan.notification.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.context.Context;

import javax.management.Notification;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class HayanCoreServiceApplication implements CommandLineRunner {


    @Autowired
    private NotificationService notificationService;

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


    @Override
    public void run(String... args) throws Exception {
        Context context = new Context();
        context.setVariable("customerName", "Ahmed Abdi");
        context.setVariable("customerEmail", "Aahosny1@gmail.com");
        context.setVariable("noOfPassengers", 1);
        context.setVariable("origin", "Jijiga");
        context.setVariable("departureDateAndTime", "2024-07-15 14:00");
        context.setVariable("destination", "Addis abeba");
        context.setVariable("arrivalDateAndTime", "2024-07-15 17:00");

        context.setVariable("flightNumber", "ET1021");
        context.setVariable("class", "ECONOMY");
        context.setVariable("ticketAmount", 3450.0);


        CompletableFuture<Void> payment_reminder = notificationService.sendMail("jamalhosny96@gmail.com", "Payment reminder", "payment-remainder-2h", context);

//        Context ucontext = new Context();
//        ucontext.setVariable("username", "ahmed");
//        ucontext.setVariable("otp", "generatedPassword");
//        ucontext.setVariable("currentYear", LocalDate.now().getYear());
//
//        CompletableFuture<Void> test = notificationService.sendMail("ahmetthosny@gmail.com", "Test", "user-credentials", ucontext);
//
//        System.out.println("test = " + test.get());


        System.out.println("payment_reminder = " + payment_reminder);

    }
}
