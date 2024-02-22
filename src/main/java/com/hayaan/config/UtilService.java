package com.hayaan.config;

import com.hayaan.flight.object.dto.flight.AirInfoResponse;
import com.hayaan.flight.object.dto.flight.PriceInfoResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UtilService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";

    private static final String ALL_CHARACTERS = UPPER + LOWER + DIGITS;


    public String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Add random characters to the password
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALL_CHARACTERS.length());
            password.append(ALL_CHARACTERS.charAt(randomIndex));
        }

        return password.toString();
    }
}
