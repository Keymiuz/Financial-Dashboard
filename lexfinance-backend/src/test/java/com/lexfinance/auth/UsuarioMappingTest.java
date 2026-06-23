package com.lexfinance.auth;

import com.lexfinance.auth.domain.Usuario;
import com.lexfinance.auth.repository.UsuarioRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.tenant.domain.Tenant;
import com.lexfinance.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class UsuarioMappingTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant sharedTenant;

    @BeforeEach
    void setUp() {
        sharedTenant = Tenant.builder()
                .nome("Tenant Teste Usuario")
                .cnpj("22333444000188")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);
    }

    @Test
    void shouldPersistUsuarioWithTenantAndAuditInfo() {
        Usuario usuario = Usuario.builder()
                .tenant(sharedTenant)
                .nome("Thiago Advocacia")
                .email("thiago@lexfinance.dev")
                .senhaHash("bcrypt_hash_placeholder")
                .build();

        Usuario saved = usuarioRepository.saveAndFlush(usuario);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isAtivo()).isTrue();
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
        assertThat(saved.getAuditInfo().getUpdatedAt()).isNotNull();
        assertThat(saved.getTenant().getId()).isEqualTo(sharedTenant.getId());
    }

    @Test
    void shouldPerformSoftDeleteOnUsuario() {
        Usuario usuario = Usuario.builder()
                .tenant(sharedTenant)
                .nome("Excluir Usuario")
                .email("delete@lexfinance.dev")
                .senhaHash("hash")
                .build();

        Usuario saved = usuarioRepository.saveAndFlush(usuario);
        UUID id = saved.getId();

        usuarioRepository.delete(saved);
        usuarioRepository.flush();

        Optional<Usuario> found = usuarioRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldEnforceUniqueEmailPerTenantConstraint() {
        Usuario user1 = Usuario.builder()
                .tenant(sharedTenant)
                .nome("User 1")
                .email("duplicate@lexfinance.dev")
                .senhaHash("hash")
                .build();
        usuarioRepository.saveAndFlush(user1);

        Usuario user2 = Usuario.builder()
                .tenant(sharedTenant)
                .nome("User 2")
                .email("duplicate@lexfinance.dev")
                .senhaHash("hash2")
                .build();

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
