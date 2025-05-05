package com.hayaan.auth.config;

import com.hayaan.auth.object.dto.TokenBody;
import com.hayaan.auth.object.entity.User;
import com.hayaan.auth.repo.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtConfig {

    @Autowired
    private UserRepository userRepository;

    //    @Value("${secret.key}")
    public static String SECRET = "6w9z7C6F8HAMcQfTjWnZr4u7xNAyDGGBKaNdRgUkXp2s5v8y2BTEVH+MbQeShVmY";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String generateToken(TokenBody tokenBody) {
        Map<String, Object> claims = new HashMap<>();
        // Construct the user object as a map
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", tokenBody.userId());
        userMap.put("roleId", tokenBody.roleId());
        userMap.put("agentId", tokenBody.agentId());
        claims.put("user", userMap);

        return createToken(claims, tokenBody.userId());
    }

    private String createToken(Map<String, Object> claims, Long userId) {

        User user = userRepository.findById(userId).get();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 100)) // 100 years
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
