package com.hayaan.config;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

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

    // generate email body

    public String emailBody(String username, String otp) {
        return "<html><body>"
                + "<h2>Dear " + username + ",</h2>"
                + "<p>Thank you for registering with us.</p>"
                + "<p>Your one-time password is: <strong>" + otp + "</strong></p>"
                + "<p>Please use this password to log in for the first time.</p>"
                + "<p>Best regards,<br/>Your Application Team</p>"
                + "</body></html>";
    }
}
