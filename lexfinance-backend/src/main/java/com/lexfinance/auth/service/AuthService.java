package com.lexfinance.auth.service;

import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.auth.dto.LoginRequest;
import com.lexfinance.auth.dto.LoginResponse;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.auth.security.JwtService;
import com.lexfinance.auth.dto.RefreshRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário inativo");
        }

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nomeUsuario(usuario.getNome())
                .tenantId(usuario.getTenant() != null ? usuario.getTenant().getId() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(RefreshRequest request) {
        try {
            String token = request.getRefreshToken();
            String email = jwtService.extractUsername(token);

            if (email == null) {
                throw new BadCredentialsException("Refresh token inválido ou expirado");
            }

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Refresh token inválido ou expirado"));

            if (!jwtService.isTokenValid(token, usuario)) {
                throw new BadCredentialsException("Refresh token inválido ou expirado");
            }

            if (!usuario.isAtivo()) {
                throw new DisabledException("Usuário inativo");
            }

            String accessToken = jwtService.generateAccessToken(usuario);
            String refreshToken = jwtService.generateRefreshToken(usuario);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .nomeUsuario(usuario.getNome())
                    .tenantId(usuario.getTenant() != null ? usuario.getTenant().getId() : null)
                    .build();
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Refresh token inválido ou expirado");
        }
    }
}
