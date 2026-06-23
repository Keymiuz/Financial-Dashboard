package com.lexfinance.auth.security;

import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.tenant.domain.Tenant;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties jwtProperties;
    private Usuario dummyUsuario;
    private Tenant dummyTenant;

    @BeforeEach
    void setUp() {
        // Inicializar propriedades de teste com segredo base64 válido (min 256 bits)
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("dGhpcy1pcy1hLXNlY3VyZS1hbmQtbG9uZy1kZXZlbG9wbWVudC1zZWNyZXQta2V5LTI1Ni1iaXRzLWZvci10ZXN0cw==");
        jwtProperties.setExpirationMs(900000L); // 15 minutos
        jwtProperties.setRefreshExpirationMs(604800000L); // 7 dias

        jwtService = new JwtService(jwtProperties);

        dummyTenant = Tenant.builder()
                .id(UUID.randomUUID())
                .nome("Tenant Teste JWT")
                .cnpj("12345678901234")
                .build();

        dummyUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .tenant(dummyTenant)
                .nome("Thiago Tech Lead")
                .email("thiago@lexfinance.dev")
                .senhaHash("bcrypt")
                .build();
    }

    @Test
    void shouldGenerateValidAccessTokenWithSubjectAndTenantClaim() {
        String token = jwtService.generateAccessToken(dummyUsuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, dummyUsuario)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(dummyUsuario.getEmail());
        assertThat(jwtService.extractTenantId(token)).isEqualTo(dummyTenant.getId());
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        String token = jwtService.generateRefreshToken(dummyUsuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token, dummyUsuario)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(dummyUsuario.getEmail());
        // Geralmente refresh token não precisa do tenantId no claim, mas deve extrair o username corretamente
    }

    @Test
    void shouldFailValidationWhenTokenIsTampered() {
        String token = jwtService.generateAccessToken(dummyUsuario);
        String tamperedToken = token + "modified";

        assertThat(jwtService.isTokenValid(tamperedToken, dummyUsuario)).isFalse();
    }

    @Test
    void shouldFailValidationWhenUsernameDoesNotMatch() {
        String token = jwtService.generateAccessToken(dummyUsuario);
        Usuario wrongUsuario = Usuario.builder()
                .email("other@lexfinance.dev")
                .build();

        assertThat(jwtService.isTokenValid(token, wrongUsuario)).isFalse();
    }
}
