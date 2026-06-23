package com.lexfinance.honorario;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.honorario.domain.ContratoHonorarios;
import com.lexfinance.honorario.domain.LancamentoHora;
import com.lexfinance.honorario.domain.TipoContrato;
import com.lexfinance.honorario.repository.ContratoHonorariosRepository;
import com.lexfinance.honorario.repository.LancamentoHoraRepository;
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
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class LancamentoHoraMappingTest {

    @Autowired
    private LancamentoHoraRepository lancamentoRepository;

    @Autowired
    private ContratoHonorariosRepository contratoRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Tenant sharedTenant;
    private ContratoHonorarios sharedContrato;

    @BeforeEach
    void setUp() {
        sharedTenant = Tenant.builder()
                .nome("Tenant Teste Lancamento")
                .cnpj("77888999000133")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);

        Cliente sharedCliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente do Lancamento")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("44455566600")
                .build();
        sharedCliente = clienteRepository.saveAndFlush(sharedCliente);

        Processo sharedProcesso = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5004444-44.2024.8.26.0100")
                .descricao("Ação de Lancamento")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();
        sharedProcesso = processoRepository.saveAndFlush(sharedProcesso);

        sharedContrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.HORA)
                .valorHora(new BigDecimal("250.00"))
                .descricao("Contrato Teste Lancamento")
                .dataContrato(LocalDate.now())
                .build();
        sharedContrato = contratoRepository.saveAndFlush(sharedContrato);
    }

    @Test
    void shouldPersistLancamentoHoraWithDefaultFaturadoFalseAndAuditInfo() {
        LancamentoHora lancamento = LancamentoHora.builder()
                .tenant(sharedTenant)
                .contrato(sharedContrato)
                .descricao("Reunião de Alinhamento")
                .data(LocalDate.now())
                .horas(new BigDecimal("2.50"))
                .valorCalculado(new BigDecimal("625.00"))
                .build();

        LancamentoHora saved = lancamentoRepository.saveAndFlush(lancamento);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isFaturado()).isFalse(); // Deve defaultar para false
        assertThat(saved.getHoras()).isEqualByComparingTo("2.50");
        assertThat(saved.getValorCalculado()).isEqualByComparingTo("625.00");
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
    }
}
