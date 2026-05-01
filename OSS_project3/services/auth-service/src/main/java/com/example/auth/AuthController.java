package com.example.auth;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JdbcClient jdbc;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public AuthController(JdbcClient jdbc, JwtService jwtService, StringRedisTemplate redisTemplate) {
        this.jdbc = jdbc;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        initSchema();
        seedAdmin();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        var existing = jdbc.sql("select count(*) from users where username = ?")
                .param(request.username())
                .query(Long.class)
                .single();
        if (existing != null && existing > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already exists");
        }
        jdbc.sql("insert into users(username, password_hash, role, full_name) values(?, ?, ?, ?)")
                .params(List.of(request.username(), hash(request.password()), "CUSTOMER", request.fullName()))
                .update();
        return issueTokens(request.username());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return issueTokens(request.username(), request.password());
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@RequestBody RefreshRequest request) {
        Claims claims = jwtService.parse(request.refreshToken());
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token type");
        }
        String storedToken = redisTemplate.opsForValue().get("refresh:" + claims.getSubject());
        if (storedToken == null || !storedToken.equals(request.refreshToken())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token mismatch");
        }
        long userId = claims.get("userId", Number.class).longValue();
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);
        return buildTokenResponse(userId, username, role);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("Authorization") String authorization) {
        Claims claims = jwtService.parse(authorization.replace("Bearer ", ""));
        return Map.of(
                "userId", claims.get("userId", Number.class).longValue(),
                "username", claims.get("username", String.class),
                "role", claims.get("role", String.class)
        );
    }

    private Map<String, Object> issueTokens(String username) {
        return issueTokens(username, null);
    }

    private Map<String, Object> issueTokens(String username, String password) {
        try {
            UserRecord user = jdbc.sql("select id, username, password_hash, role, full_name from users where username = ?")
                    .param(username)
                    .query((rs, rowNum) -> new UserRecord(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("full_name")))
                    .single();
            if (password != null && !user.passwordHash().equals(hash(password))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
            }
            return buildTokenResponse(user.id(), user.username(), user.role());
        } catch (EmptyResultDataAccessException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
    }

    private Map<String, Object> buildTokenResponse(long userId, String username, String role) {
        String accessToken = jwtService.createAccessToken(userId, username, role);
        String refreshToken = jwtService.createRefreshToken(userId, username, role);
        redisTemplate.opsForValue().set("refresh:" + username, refreshToken);
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", Map.of("id", userId, "username", username, "role", role)
        );
    }

    private void initSchema() {
        jdbc.sql("""
                create table if not exists users (
                  id integer primary key autoincrement,
                  username text not null unique,
                  password_hash text not null,
                  role text not null,
                  full_name text
                )
                """).update();
    }

    private void seedAdmin() {
        Long count = jdbc.sql("select count(*) from users where username = 'admin'")
                .query(Long.class)
                .single();
        if (count != null && count == 0) {
            jdbc.sql("insert into users(username, password_hash, role, full_name) values(?, ?, ?, ?)")
                    .params(List.of("admin", hash("admin1234"), "ADMIN", "Restaurant Admin"))
                    .update();
        }
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    record RegisterRequest(String username, String password, String fullName) {}
    record LoginRequest(String username, String password) {}
    record RefreshRequest(String refreshToken) {}
    record UserRecord(long id, String username, String passwordHash, String role, String fullName) {}
}
