package com.lexfinance.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtClaimsTest {

    private static final String SECRET = "dGhpcy1pcy1hLXNlY3VyZS1hbmQtbG9uZy1kZXZlbG9wbWVudC1zZWNyZXQta2V5LTI1Ni1iaXRzLWZvci10ZXN0cw==";
    private SecretKey key;
    private UUID expectedTenantId;
    private String expectedEmail;

    @BeforeEach
    void setUp() {
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        expectedTenantId = UUID.randomUUID();
        expectedEmail = "admin@lexfinance.dev";
    }

    @Test
    void shouldCreateAndParseTokenWithCustomClaims() {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 1000 * 60 * 15); // 15 mins

        // Generates token manually with custom claims
        String token = Jwts.builder()
                .subject(expectedEmail)
                .claim("tenantId", expectedTenantId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();

        // Parses claims using Jwts parser
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(expectedEmail);
        assertThat(claims.get("tenantId", String.class)).isEqualTo(expectedTenantId.toString());
        assertThat(claims.getExpiration()).isAfter(now);
    }
}
