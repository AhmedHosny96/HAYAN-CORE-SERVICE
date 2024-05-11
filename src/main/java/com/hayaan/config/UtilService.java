package com.hayaan.config;

import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class UtilService {

    public String generatePassword() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int length = 8;

        StringBuilder otp = new StringBuilder();

        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());

            otp.append(characters.charAt(index));
        }

        return otp.toString();
    }

    // convert string minutes into duration

    public Duration convertStringToDuration(int totalMinutesString) {
        return Duration.ofMinutes(totalMinutesString);
    }
}
