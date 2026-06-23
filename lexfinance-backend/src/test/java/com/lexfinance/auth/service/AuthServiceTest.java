package com.lexfinance.auth.service;

import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.auth.dto.LoginRequest;
import com.lexfinance.auth.dto.LoginResponse;
import com.lexfinance.auth.dto.RefreshRequest;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.auth.security.JwtService;
import com.lexfinance.tenant.domain.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Tenant dummyTenant;
    private Usuario activeUsuario;
    private Usuario inactiveUsuario;

    @BeforeEach
    void setUp() {
        dummyTenant = Tenant.builder()
                .id(UUID.randomUUID())
                .nome("Tenant Teste Auth")
                .cnpj("12345678901234")
                .build();

        activeUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .tenant(dummyTenant)
                .nome("Thiago Tech Lead")
                .email("thiago@lexfinance.dev")
                .senhaHash("encoded_bcrypt")
                .ativo(true)
                .build();

        inactiveUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .tenant(dummyTenant)
                .nome("Inativo")
                .email("inativo@lexfinance.dev")
                .senhaHash("encoded_bcrypt")
                .ativo(false)
                .build();
    }

    @Test
    void shouldLoginSuccessfullyAndReturnTokens() {
        LoginRequest request = new LoginRequest("thiago@lexfinance.dev", "senha123");

        when(usuarioRepository.findByEmail("thiago@lexfinance.dev")).thenReturn(Optional.of(activeUsuario));
        when(passwordEncoder.matches("senha123", "encoded_bcrypt")).thenReturn(true);
        when(jwtService.generateAccessToken(activeUsuario)).thenReturn("mocked_access_token");
        when(jwtService.generateRefreshToken(activeUsuario)).thenReturn("mocked_refresh_token");

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("mocked_refresh_token");
        assertThat(response.getNomeUsuario()).isEqualTo("Thiago Tech Lead");
        assertThat(response.getTenantId()).isEqualTo(dummyTenant.getId());
    }

    @Test
    void shouldFailLoginWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@lexfinance.dev", "senha123");

        when(usuarioRepository.findByEmail("unknown@lexfinance.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void shouldFailLoginWhenPasswordIncorrect() {
        LoginRequest request = new LoginRequest("thiago@lexfinance.dev", "wrong_password");

        when(usuarioRepository.findByEmail("thiago@lexfinance.dev")).thenReturn(Optional.of(activeUsuario));
        when(passwordEncoder.matches("wrong_password", "encoded_bcrypt")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void shouldFailLoginWhenUserIsInactive() {
        LoginRequest request = new LoginRequest("inativo@lexfinance.dev", "senha123");

        when(usuarioRepository.findByEmail("inativo@lexfinance.dev")).thenReturn(Optional.of(inactiveUsuario));
        when(passwordEncoder.matches("senha123", "encoded_bcrypt")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DisabledException.class)
                .hasMessage("Usuário inativo");
    }

    @Test
    void shouldRefreshSuccessfullyAndReturnNewTokens() {
        RefreshRequest request = new RefreshRequest("valid_refresh_token");

        when(jwtService.extractUsername("valid_refresh_token")).thenReturn("thiago@lexfinance.dev");
        when(usuarioRepository.findByEmail("thiago@lexfinance.dev")).thenReturn(Optional.of(activeUsuario));
        when(jwtService.isTokenValid("valid_refresh_token", activeUsuario)).thenReturn(true);
        when(jwtService.generateAccessToken(activeUsuario)).thenReturn("new_access_token");
        when(jwtService.generateRefreshToken(activeUsuario)).thenReturn("new_refresh_token");

        LoginResponse response = authService.refresh(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
        assertThat(response.getNomeUsuario()).isEqualTo("Thiago Tech Lead");
        assertThat(response.getTenantId()).isEqualTo(dummyTenant.getId());
    }

    @Test
    void shouldFailRefreshWhenTokenInvalid() {
        RefreshRequest request = new RefreshRequest("invalid_refresh_token");

        when(jwtService.extractUsername("invalid_refresh_token")).thenReturn("thiago@lexfinance.dev");
        when(usuarioRepository.findByEmail("thiago@lexfinance.dev")).thenReturn(Optional.of(activeUsuario));
        when(jwtService.isTokenValid("invalid_refresh_token", activeUsuario)).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token inválido ou expirado");
    }

    @Test
    void shouldFailRefreshWhenUserNotFound() {
        RefreshRequest request = new RefreshRequest("valid_refresh_token");

        when(jwtService.extractUsername("valid_refresh_token")).thenReturn("unknown@lexfinance.dev");
        when(usuarioRepository.findByEmail("unknown@lexfinance.dev")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Refresh token inválido ou expirado");
    }

    @Test
    void shouldFailRefreshWhenUserIsInactive() {
        RefreshRequest request = new RefreshRequest("valid_refresh_token");

        when(jwtService.extractUsername("valid_refresh_token")).thenReturn("inativo@lexfinance.dev");
        when(usuarioRepository.findByEmail("inativo@lexfinance.dev")).thenReturn(Optional.of(inactiveUsuario));
        when(jwtService.isTokenValid("valid_refresh_token", inactiveUsuario)).thenReturn(true);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(DisabledException.class)
                .hasMessage("Usuário inativo");
    }
}
