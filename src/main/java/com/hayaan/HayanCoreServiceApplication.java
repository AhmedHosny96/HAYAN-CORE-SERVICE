package com.hayaan;

import com.google.zxing.WriterException;
import com.hayaan.flight.repo.CurrencyRepository;
import com.hayaan.flight.service.FlightLogicService;
import com.hayaan.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
@EntityScan(basePackageClasses = {HayanCoreServiceApplication.class})
public class HayanCoreServiceApplication implements CommandLineRunner {


    @Bean
    public ModelMapper modelMapper() {

        return new ModelMapper();
    }
// 140892 -> 232878


//    @PostConstruct
//    public void insertCurrencies() {
//
//        List<Currency> currencies = Arrays.asList(
//                new Currency("Ethiopian Birr", "ETB"),
//                new Currency("US Dollar", "USD"),
//                new Currency("Euro", "EUR"),
//                new Currency("British Pound", "GBP"),
//                new Currency("Japanese Yen", "JPY"),
//                new Currency("Swiss Franc", "CHF"),
//                new Currency("Canadian Dollar", "CAD"),
//                new Currency("Australian Dollar", "AUD"),
//                new Currency("New Zealand Dollar", "NZD"),
//                new Currency("Chinese Yuan", "CNY"),
//                new Currency("Indian Rupee", "INR")
//        );
//
//        // Save all currencies to the database
//        currencyRepository.saveAll(currencies);
//    }

    public static void main(String[] args) throws IOException, WriterException {

        SpringApplication.run(HayanCoreServiceApplication.class, args);


    }


    @Override
    public void run(String... args) throws Exception {

//        JSONObject jsonObject = flightLogicService.fetchTripDetails("TR27522024");
//
//        JSONObject itineraryInfo = jsonObject.optJSONObject("ItineraryInfo");
//
//        log.info("customerInfo : {}", itineraryInfo.optJSONArray("CustomerInfos").optJSONObject(0).optJSONObject("CustomerInfo"));
//
//        log.info("reservation item : {}", itineraryInfo.optJSONArray("ReservationItems").optJSONObject(0).optJSONObject("ReservationItem"));
    }
}
