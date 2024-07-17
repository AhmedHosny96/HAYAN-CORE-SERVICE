package com.hayaan.auth.service;


import com.hayaan.auth.config.JwtConfig;
import com.hayaan.auth.object.dto.TokenBody;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import com.hayaan.dto.CustomResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PartnerAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtConfig jwtConfig;


    public CustomResponse authenticatePartner(String username) {

        Optional<User> byUsername = userRepository.findByUsername(username);

        if (!byUsername.isPresent()) {
            return new CustomResponse(403, "Error", "Invalid username");
        }

        User user = byUsername.get();

        TokenBody tokenBody = new TokenBody(username, "PARTNER", user.getId(), null, user.getStatus());

        String token = jwtConfig.generateToken(tokenBody);

        return new CustomResponse(200, "success", token);
    }
}
