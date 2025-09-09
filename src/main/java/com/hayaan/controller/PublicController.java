package com.hayaan.controller;

import com.hayaan.auth.object.dto.GoogleAuthDto;
import com.hayaan.auth.object.dto.NewsletterSubscriptionDto;
import com.hayaan.auth.service.UserService;
import com.hayaan.dto.CustomResponse;
import com.hayaan.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class PublicController {

    private final NewsletterService newsletterService;
    private final UserService userService;

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<CustomResponse> subscribeToNewsletter(@RequestBody NewsletterSubscriptionDto subscriptionDto) {
        CustomResponse response = newsletterService.subscribeToNewsletter(subscriptionDto);
        return ResponseEntity.status(response.status()).body(response);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<CustomResponse> googleAuthentication(@RequestBody GoogleAuthDto googleAuthDto) {
        CustomResponse response = userService.authenticateWithGoogle(googleAuthDto);
        return ResponseEntity.status(response.status()).body(response);
    }
} 