package com.hayaan.auth.controller;

import com.hayaan.auth.config.JwtConfig;
import com.hayaan.auth.object.dto.AuthRequestDto;
import com.hayaan.auth.object.dto.AuthResponse;
import com.hayaan.auth.object.dto.TokenBody;
import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.repo.RoleRepo;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.auth.service.PartnerAuthService;
import com.hayaan.dto.CustomResponse;
import com.hayaan.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class AuthController {

    private final JwtConfig jwtService;

    private final UserRepository userRepository;

    private final RoleRepo roleRepo;

    private final PartnerAuthService partnerAuthService;

    private final NotificationService notificationService;

    private final AuthenticationManager authenticationManager;


    @GetMapping("/test")
    public void testEmail(@RequestParam String email) throws ExecutionException, InterruptedException {

        Context context = new Context();
        context.setVariable("username", "Ahmed");
        context.setVariable("otp", "generatedPassword");
        context.setVariable("currentYear", LocalDate.now().getYear());

        CompletableFuture<Void> completableFuture = notificationService.sendMail(email, "Onetime password", "user-credentials", context, Optional.empty());

        log.info("completableFuture : {}", completableFuture);
    }

    //

    @GetMapping("/browse")
    public ResponseEntity<?> browse(@RequestParam String username) {

        CustomResponse customResponse = partnerAuthService.authenticatePartner(username);
        return ResponseEntity.status(customResponse.status()).body(customResponse);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> auth(@RequestBody AuthRequestDto authRequest) {

        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password()
        ));

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("invalid username or password");
        }

        // add role , agentId , status to token

        var currentUser = userRepository.findByUsername(authRequest.username()).get();

        // curent user role

        Role role = roleRepo.findById(currentUser.getRole().getId()).get();

        var tokenBody = new TokenBody(currentUser.getRole().getId(), currentUser.getId(), currentUser.getAgent() == null ? null : currentUser.getAgent().getId());

        String token = jwtService.generateToken(tokenBody);

        var customResponse = new AuthResponse(
                200,
                "success",
                authRequest.username(),
                token,
                currentUser.isPasswordChanged(),
                role.getName(),
                currentUser.getId()
        );
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    @GetMapping("/google")
    public ResponseEntity<Void> googleLogin() {
        return ResponseEntity.status(302)
                .location(URI.create("/oauth2/authorization/google"))
                .build();
    }


}
