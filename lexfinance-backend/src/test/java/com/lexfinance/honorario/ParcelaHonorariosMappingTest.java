package com.lexfinance.honorario;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.honorario.domain.ContratoHonorarios;
import com.lexfinance.honorario.domain.ParcelaHonorarios;
import com.lexfinance.honorario.domain.StatusParcela;
import com.lexfinance.honorario.domain.TipoContrato;
import com.lexfinance.honorario.repository.ContratoHonorariosRepository;
import com.lexfinance.honorario.repository.ParcelaHonorariosRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class ParcelaHonorariosMappingTest {

    @Autowired
    private ParcelaHonorariosRepository parcelaRepository;

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
                .nome("Tenant Teste Parcela")
                .cnpj("66777888000144")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);

        Cliente sharedCliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente da Parcela")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("33344455500")
                .build();
        sharedCliente = clienteRepository.saveAndFlush(sharedCliente);

        Processo sharedProcesso = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5003333-33.2024.8.26.0100")
                .descricao("Ação de Parcela")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();
        sharedProcesso = processoRepository.saveAndFlush(sharedProcesso);

        sharedContrato = ContratoHonorarios.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .tipo(TipoContrato.FIXO)
                .valorFixo(new BigDecimal("5000.00"))
                .descricao("Contrato Teste Parcela")
                .dataContrato(LocalDate.now())
                .build();
        sharedContrato = contratoRepository.saveAndFlush(sharedContrato);
    }

    @Test
    void shouldPersistParcelaPendenteWithAuditInfo() {
        ParcelaHonorarios parcela = ParcelaHonorarios.builder()
                .tenant(sharedTenant)
                .contrato(sharedContrato)
                .numeroParcela(1)
                .valor(new BigDecimal("1250.00"))
                .dataVencimento(LocalDate.now().plusDays(30))
                .status(StatusParcela.PENDENTE)
                .build();

        ParcelaHonorarios saved = parcelaRepository.saveAndFlush(parcela);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValor()).isEqualByComparingTo("1250.00");
        assertThat(saved.getStatusCalculado()).isEqualTo(StatusParcela.PENDENTE);
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistParcelaRecebidaWithDataRecebimento() {
        ParcelaHonorarios parcela = ParcelaHonorarios.builder()
                .tenant(sharedTenant)
                .contrato(sharedContrato)
                .numeroParcela(1)
                .valor(new BigDecimal("1250.00"))
                .dataVencimento(LocalDate.now().minusDays(5))
                .status(StatusParcela.RECEBIDO)
                .dataRecebimento(LocalDate.now().minusDays(6))
                .build();

        ParcelaHonorarios saved = parcelaRepository.saveAndFlush(parcela);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatusCalculado()).isEqualTo(StatusParcela.RECEBIDO);
        assertThat(saved.getDataRecebimento()).isNotNull();
    }

    @Test
    void shouldCalculateStatusAtrasadoWhenPendenteAndPastDueDate() {
        ParcelaHonorarios parcela = ParcelaHonorarios.builder()
                .tenant(sharedTenant)
                .contrato(sharedContrato)
                .numeroParcela(1)
                .valor(new BigDecimal("1250.00"))
                .dataVencimento(LocalDate.now().minusDays(1)) // Venceu ontem
                .status(StatusParcela.PENDENTE)
                .build();

        ParcelaHonorarios saved = parcelaRepository.saveAndFlush(parcela);

        assertThat(saved.getStatus()).isEqualTo(StatusParcela.PENDENTE); // No banco continua PENDENTE
        assertThat(saved.getStatusCalculado()).isEqualTo(StatusParcela.ATRASADO); // Em runtime calcula ATRASADO
    }

    @Test
    void shouldFailWhenStatusIsRecebidoAndDataRecebimentoIsNull() {
        ParcelaHonorarios parcela = ParcelaHonorarios.builder()
                .tenant(sharedTenant)
                .contrato(sharedContrato)
                .numeroParcela(1)
                .valor(new BigDecimal("1250.00"))
                .dataVencimento(LocalDate.now().plusDays(10))
                .status(StatusParcela.RECEBIDO)
                .dataRecebimento(null) // Viola regra de banco de dados
                .build();

        assertThatThrownBy(() -> parcelaRepository.saveAndFlush(parcela))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
