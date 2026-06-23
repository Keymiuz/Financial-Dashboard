package com.lexfinance.auth.security;

import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID expectedTenantId;
    private Usuario expectedUsuario;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
        expectedTenantId = UUID.randomUUID();
        expectedUsuario = Usuario.builder()
                .email("admin@lexfinance.dev")
                .ativo(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void shouldAuthenticateWhenValidTokenProvided() throws ServletException, IOException {
        String token = "valid_token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("admin@lexfinance.dev");
        when(usuarioRepository.findByEmail("admin@lexfinance.dev")).thenReturn(Optional.of(expectedUsuario));
        when(jwtService.isTokenValid(token, expectedUsuario)).thenReturn(true);
        when(jwtService.extractTenantId(token)).thenReturn(expectedTenantId);

        // Assert that context is set DURING the execution of the filter chain
        doAnswer(invocation -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin@lexfinance.dev");
            assertThat(TenantContext.getCurrentTenant()).isEqualTo(expectedTenantId);
            return null;
        }).when(filterChain).doFilter(request, response);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert that context is cleared AFTER doFilterInternal completes
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenNoHeaderProvided() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenInvalidTokenProvided() throws ServletException, IOException {
        String token = "invalid_token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("admin@lexfinance.dev");
        when(usuarioRepository.findByEmail("admin@lexfinance.dev")).thenReturn(Optional.of(expectedUsuario));
        when(jwtService.isTokenValid(token, expectedUsuario)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(TenantContext.getCurrentTenant()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
