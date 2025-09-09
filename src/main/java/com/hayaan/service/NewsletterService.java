package com.hayaan.service;

import com.hayaan.auth.object.dto.NewsletterSubscriptionDto;
import com.hayaan.auth.object.entity.Newsletter;
import com.hayaan.auth.repo.NewsletterRepository;
import com.hayaan.dto.CustomResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;

    public CustomResponse subscribeToNewsletter(NewsletterSubscriptionDto subscriptionDto) {
        try {
            if (subscriptionDto.getEmail() == null || subscriptionDto.getEmail().trim().isEmpty()) {
                return new CustomResponse(400, "Email is required", null);
            }

            // Basic email validation
            if (!isValidEmail(subscriptionDto.getEmail())) {
                return new CustomResponse(400, "Invalid email format", null);
            }

            // Check if email already exists
            if (newsletterRepository.existsByEmail(subscriptionDto.getEmail())) {
                return new CustomResponse(409, "Email already subscribed to newsletter", null);
            }

            // Create newsletter subscription
            Newsletter newsletter = Newsletter.builder()
                    .email(subscriptionDto.getEmail().toLowerCase().trim())
                    .status(1)
                    .createdDate(LocalDateTime.now())
                    .build();

            newsletterRepository.save(newsletter);

            log.info("New newsletter subscription: {}", subscriptionDto.getEmail());

            return new CustomResponse(200, "Successfully subscribed to newsletter", null);

        } catch (Exception e) {
            log.error("Error subscribing to newsletter", e);
            return new CustomResponse(500, "Unable to subscribe to newsletter", null);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
} 