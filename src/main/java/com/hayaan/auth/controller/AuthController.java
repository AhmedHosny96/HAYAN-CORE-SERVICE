package com.hayaan.auth.controller;

import com.hayaan.auth.config.JwtConfig;
import com.hayaan.auth.object.dto.AuthRequestDto;
import com.hayaan.auth.object.dto.AuthResponse;
import com.hayaan.auth.object.dto.TokenBody;
import com.hayaan.auth.object.entity.Role;
import com.hayaan.auth.repo.RoleRepo;
import com.hayaan.auth.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
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

    private final UserRepository userRepository;

    private final RoleRepo roleRepo;

    private final AuthenticationManager authenticationManager;

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

        var tokenBody = new TokenBody(currentUser.getUsername(), role.getName(), currentUser.getId(), currentUser.getAgent().getId(), currentUser.getStatus());
        
        String token = jwtService.generateToken(tokenBody);

        var customResponse = new AuthResponse(
                200,
                "success",
                authRequest.username(),
                token
        );
        return new ResponseEntity<>(customResponse, HttpStatus.OK);

    }
}
