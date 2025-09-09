package com.hayaan.auth.config;

import com.hayaan.auth.object.dto.TokenBody;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtConfig jwt;
    private final UserRepository users;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) auth.getPrincipal();
        String email = principal.getAttribute("email");
        User u = users.findByEmail(email).orElseThrow();

        // Build TokenBody exactly as your generator expects
        Integer roleId = (u.getRole() != null && u.getRole().getId() != null)
                ? u.getRole().getId().intValue() : null;

        Long agentId = (u.getAgent() != null) ? u.getAgent().getId() : null;

        TokenBody tokenBody = new TokenBody(
                roleId,
                u.getId(),
                agentId
        );

        String token = jwt.generateToken(tokenBody);

        Cookie cookie = new Cookie("APP_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(req.isSecure());
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        res.addCookie(cookie);
        getRedirectStrategy().sendRedirect(req, res, "https://dev.hayaantravel.com?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
    }
}
