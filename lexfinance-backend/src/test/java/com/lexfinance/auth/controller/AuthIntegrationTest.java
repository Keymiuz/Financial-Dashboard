package com.lexfinance.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.auth.dto.LoginRequest;
import com.lexfinance.auth.dto.RefreshRequest;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.auth.security.JwtService;
import com.lexfinance.config.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.context.annotation.Import(AuthIntegrationTest.IntegrationTestTenantController.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void testDirect() {
        Usuario usuario = usuarioRepository.findByEmail("admin@lexfinance.dev").orElse(null);
        assertThat(usuario).isNotNull();
        assertThat(passwordEncoder.matches("admin123", usuario.getSenhaHash())).isTrue();
        String token = jwtService.generateAccessToken(usuario);
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@lexfinance.dev");
        assertThat(jwtService.extractTenantId(token)).isEqualTo(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        assertThat(jwtService.isTokenValid(token, usuario)).isTrue();
    }

    @Test
    void shouldPerformFullAuthFlowAndVerifyTenantContext() throws Exception {
        // 1. Login Válido
        LoginRequest loginRequest = new LoginRequest("admin@lexfinance.dev", "admin123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.nomeUsuario").value("Administrador LexFinance"))
                .andExpect(jsonPath("$.tenantId").value("550e8400-e29b-41d4-a716-446655440000"))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(responseBody).get("refreshToken").asText();

        // 2. Acesso com Tenant Correto no context
        mockMvc.perform(get("/test/tenant-context-check")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("550e8400-e29b-41d4-a716-446655440000"));

        // 3. Refresh Token Válido
        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        // Login Inválido
        LoginRequest loginRequest = new LoginRequest("admin@lexfinance.dev", "wrong_password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectExpiredOrInvalidToken() throws Exception {
        // Token inválido/expirado para rota protegida
        mockMvc.perform(get("/test/tenant-context-check")
                        .header("Authorization", "Bearer invalid_or_expired_token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidOrRevokedRefreshToken() throws Exception {
        // Refresh token inválido / "revogado"
        RefreshRequest refreshRequest = new RefreshRequest("invalid_or_revoked_refresh_token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    // Static nested controller for testing TenantContext propagation during actual request dispatching
    @RestController
    @RequestMapping("/test")
    static class IntegrationTestTenantController {
        @GetMapping("/tenant-context-check")
        public String checkTenant() {
            UUID tenantId = TenantContext.getCurrentTenant();
            return "{\"tenantId\":\"" + (tenantId != null ? tenantId.toString() : "null") + "\"}";
        }
    }
}
