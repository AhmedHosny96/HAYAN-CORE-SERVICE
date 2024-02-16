package com.hayaan.auth.controller;

import com.hayaan.auth.config.JwtConfig;
import com.hayaan.auth.object.dto.AuthRequestDto;
import com.hayaan.auth.object.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtConfig jwtService;

    private final AuthenticationManager authenticationManager;

    @PostMapping(value = "/login")
    public ResponseEntity<?> auth(@RequestBody AuthRequestDto authRequest) {

        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.username(), authRequest.password()
        ));

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("invalid username or password");
        }

        String token = jwtService.generateToken(authRequest.username());
        var customResponse = new AuthResponse(
                200,
                "success",
                authRequest.username(),
                token
        );
        return new ResponseEntity<>(customResponse, HttpStatus.OK);

    }
}
