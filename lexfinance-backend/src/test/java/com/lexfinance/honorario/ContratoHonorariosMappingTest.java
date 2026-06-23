package com.lexfinance.honorario;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.honorario.domain.ContratoHonorarios;
import com.lexfinance.honorario.domain.TipoContrato;
import com.lexfinance.honorario.repository.ContratoHonorariosRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class ContratoHonorariosMappingTest {

    @Autowired
    private ContratoHonorariosRepository contratoRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Tenant sharedTenant;
    private Processo sharedProcesso;

    @BeforeEach
    void setUp() {
        sharedTenant = Tenant.builder()
                .nome("Tenant Teste Contrato")
                .cnpj("55666777000155")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);

        Cliente sharedCliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente do Contrato")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("22233344400")
                .build();
        sharedCliente = clienteRepository.saveAndFlush(sharedCliente);

        sharedProcesso = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5002222-22.2024.8.26.0100")
                .descricao("Ação de Contrato")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();
        sharedProcesso = processoRepository.saveAndFlush(sharedProcesso);
    }

    @Test
    void shouldPersistContratoFixoWithAuditInfo() {
        ContratoHonorarios contrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.FIXO)
                .valorFixo(new BigDecimal("5000.00"))
                .descricao("Contrato de Honorários Fixos")
                .dataContrato(LocalDate.now())
                .build();

        ContratoHonorarios saved = contratoRepository.saveAndFlush(contrato);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isAtivo()).isTrue();
        assertThat(saved.getValorFixo()).isEqualByComparingTo("5000.00");
        assertThat(saved.getValorHora()).isNull();
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistContratoHoraWithAuditInfo() {
        ContratoHonorarios contrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.HORA)
                .valorHora(new BigDecimal("250.00"))
                .descricao("Contrato por Hora de Trabalho")
                .dataContrato(LocalDate.now())
                .build();

        ContratoHonorarios saved = contratoRepository.saveAndFlush(contrato);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValorHora()).isEqualByComparingTo("250.00");
        assertThat(saved.getValorFixo()).isNull();
    }

    @Test
    void shouldPerformSoftDeleteOnContrato() {
        ContratoHonorarios contrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.FIXO)
                .valorFixo(new BigDecimal("3000.00"))
                .descricao("Contrato a Excluir")
                .dataContrato(LocalDate.now())
                .build();

        ContratoHonorarios saved = contratoRepository.saveAndFlush(contrato);
        UUID id = saved.getId();

        contratoRepository.delete(saved);
        contratoRepository.flush();

        Optional<ContratoHonorarios> found = contratoRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFailWhenContratoFixoHasValorHoraPopulated() {
        ContratoHonorarios contrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.FIXO)
                .valorFixo(new BigDecimal("5000.00"))
                .valorHora(new BigDecimal("250.00")) // Viola regra de exclusão mútua
                .descricao("Contrato Inválido")
                .dataContrato(LocalDate.now())
                .build();

        assertThatThrownBy(() -> contratoRepository.saveAndFlush(contrato))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
