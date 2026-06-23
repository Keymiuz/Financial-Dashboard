package com.lexfinance.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexfinance.auth.dto.LoginRequest;
import com.lexfinance.auth.dto.LoginResponse;
import com.lexfinance.auth.dto.RefreshRequest;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.auth.security.JwtAuthenticationFilter;
import com.lexfinance.auth.security.JwtService;
import com.lexfinance.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Required by SecurityConfig/JwtAuthenticationFilter setup
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    @Test
    @WithMockUser
    void shouldReturn200AndTokensOnValidLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@lexfinance.dev", "admin123");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .nomeUsuario("Admin")
                .tenantId(UUID.randomUUID())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access_token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
                .andExpect(jsonPath("$.nomeUsuario").value("Admin"));
    }

    @Test
    @WithMockUser
    void shouldReturn401OnInvalidLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@lexfinance.dev", "wrong_password");

        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldReturn200AndNewTokensOnValidRefresh() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest("valid_refresh_token");
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .nomeUsuario("Admin")
                .tenantId(UUID.randomUUID())
                .build();

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh_token"));
    }
}
