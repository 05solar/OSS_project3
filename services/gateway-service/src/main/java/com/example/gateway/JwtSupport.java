package com.example.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSupport {
    private final SecretKey key;

    public JwtSupport(@Value("${app.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.repeat(4).substring(0, 32).getBytes(StandardCharsets.UTF_8));
    }

    public UserContext parse(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new UserContext(
                claims.get("userId", Number.class).longValue(),
                claims.get("username", String.class),
                claims.get("role", String.class),
                token
        );
    }

    public record UserContext(long userId, String username, String role, String token) {}
}
