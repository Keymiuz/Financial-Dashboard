package com.lexfinance.processo;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.processo.domain.AreaProcesso;
import com.lexfinance.processo.domain.Processo;
import com.lexfinance.processo.domain.StatusProcesso;
import com.lexfinance.processo.repository.ProcessoRepository;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class ProcessoMappingTest {

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Tenant sharedTenant;
    private Cliente sharedCliente;

    @BeforeEach
    void setUp() {
        sharedTenant = Tenant.builder()
                .nome("Tenant Teste Processo")
                .cnpj("44555666000166")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);

        sharedCliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente do Processo")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("11122233300")
                .build();
        sharedCliente = clienteRepository.saveAndFlush(sharedCliente);
    }

    @Test
    void shouldPersistProcessoWithGeneratedIdAndAuditInfo() {
        Processo processo = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5001234-56.2024.8.26.0100")
                .descricao("Ação Civil Ordinária")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();

        Processo saved = processoRepository.saveAndFlush(processo);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
        assertThat(saved.getTenant().getId()).isEqualTo(sharedTenant.getId());
        assertThat(saved.getCliente().getId()).isEqualTo(sharedCliente.getId());
    }

    @Test
    void shouldEnforceUniqueCnjPerTenantConstraint() {
        Processo processo1 = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5001234-56.2024.8.26.0100")
                .descricao("Primeiro Processo")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();
        processoRepository.saveAndFlush(processo1);

        Processo processo2 = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5001234-56.2024.8.26.0100")
                .descricao("Segundo Processo Duplicado")
                .area(AreaProcesso.TRIBUTARIO)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();

        assertThatThrownBy(() -> processoRepository.saveAndFlush(processo2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
