package com.hayaan.auth.config;

import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Oauth2UserService implements OAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oauth = new DefaultOAuth2UserService().loadUser(req);
        String provider = "google";
        String sub = oauth.getAttribute("sub");
        String email = oauth.getAttribute("email");
        String name = oauth.getAttribute("name");
        String pic = oauth.getAttribute("picture");
        Boolean verified = oauth.getAttribute("email_verified");

        User u = userRepository.findByProviderAndProviderId(provider, sub)
                .orElseGet(() -> userRepository.findByEmail(email).orElse(User.builder().email(email).build()));

        u.setUsername(u.getUsername() != null ? u.getUsername() : email);
        u.setFullName(name);
        u.setPictureUrl(pic);
        u.setProvider(provider);
        u.setProviderId(sub);
        u.setEmailVerified(Boolean.TRUE.equals(verified));
        u.setCreatedDate(LocalDateTime.now());
        u.setLastLoginAt(LocalDateTime.now());
        userRepository.save(u);

        System.out.println("==== USER ====" + u);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + (u.getRole()!=null?u.getRole().getName():"USER"))),
                oauth.getAttributes(),
                "sub"
        );
    }
}
