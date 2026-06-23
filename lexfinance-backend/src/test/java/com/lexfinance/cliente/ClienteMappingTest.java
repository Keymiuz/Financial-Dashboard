package com.lexfinance.cliente;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
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
class ClienteMappingTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant sharedTenant;

    @BeforeEach
    void setUp() {
        sharedTenant = Tenant.builder()
                .nome("Tenant Teste Cliente")
                .cnpj("33444555000177")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);
    }

    @Test
    void shouldPersistClienteWithGeneratedIdAndAuditInfo() {
        Cliente cliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Empresa Cliente Exemplo")
                .tipo(TipoCliente.PESSOA_JURIDICA)
                .cpfCnpj("12345678000100")
                .email("cliente@email.com")
                .telefone("(11) 99999-8888")
                .build();

        Cliente saved = clienteRepository.saveAndFlush(cliente);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isAtivo()).isTrue();
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
        assertThat(saved.getTenant().getId()).isEqualTo(sharedTenant.getId());
    }

    @Test
    void shouldPerformSoftDeleteOnCliente() {
        Cliente cliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente Excluir")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("98765432100")
                .build();

        Cliente saved = clienteRepository.saveAndFlush(cliente);
        UUID id = saved.getId();

        clienteRepository.delete(saved);
        clienteRepository.flush();

        Optional<Cliente> found = clienteRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldEnforceUniqueCpfCnpjPerTenantConstraint() {
        Cliente cliente1 = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente 1")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("11122233344")
                .build();
        clienteRepository.saveAndFlush(cliente1);

        Cliente cliente2 = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente 2")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("11122233344")
                .build();

        assertThatThrownBy(() -> clienteRepository.saveAndFlush(cliente2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
