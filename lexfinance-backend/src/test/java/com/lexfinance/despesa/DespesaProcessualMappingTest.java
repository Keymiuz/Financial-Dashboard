package com.lexfinance.despesa;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.domain.TipoCliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.JpaAuditingConfig;
import com.lexfinance.despesa.domain.DespesaProcessual;
import com.lexfinance.despesa.domain.StatusRessarcimento;
import com.lexfinance.despesa.domain.TipoDespesa;
import com.lexfinance.despesa.repository.DespesaProcessualRepository;
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
class DespesaProcessualMappingTest {

    @Autowired
    private DespesaProcessualRepository despesaRepository;

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
                .nome("Tenant Teste Despesa")
                .cnpj("88999000000122")
                .build();
        sharedTenant = tenantRepository.saveAndFlush(sharedTenant);

        Cliente sharedCliente = Cliente.builder()
                .tenant(sharedTenant)
                .nome("Cliente da Despesa")
                .tipo(TipoCliente.PESSOA_FISICA)
                .cpfCnpj("55566677700")
                .build();
        sharedCliente = clienteRepository.saveAndFlush(sharedCliente);

        sharedProcesso = Processo.builder()
                .tenant(sharedTenant)
                .cliente(sharedCliente)
                .numeroCnj("5005555-55.2024.8.26.0100")
                .descricao("Ação de Despesa")
                .area(AreaProcesso.CIVIL)
                .status(StatusProcesso.ATIVO)
                .dataInicio(LocalDate.now())
                .build();
        sharedProcesso = processoRepository.saveAndFlush(sharedProcesso);
    }

    @Test
    void shouldPersistDespesaPendenteWithAuditInfo() {
        DespesaProcessual despesa = DespesaProcessual.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .descricao("Cópia de Processo Físico")
                .tipo(TipoDespesa.CUSTA_JUDICIAL)
                .valor(new BigDecimal("150.00"))
                .dataDespesa(LocalDate.now())
                .statusRessarcimento(StatusRessarcimento.PENDENTE)
                .build();

        DespesaProcessual saved = despesaRepository.saveAndFlush(despesa);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getValor()).isEqualByComparingTo("150.00");
        assertThat(saved.getStatusRessarcimento()).isEqualTo(StatusRessarcimento.PENDENTE);
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistDespesaRessarcidaWithDataRessarcimento() {
        DespesaProcessual despesa = DespesaProcessual.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .descricao("Custas de Postagem Postal")
                .tipo(TipoDespesa.CUSTA_JUDICIAL)
                .valor(new BigDecimal("45.50"))
                .dataDespesa(LocalDate.now().minusDays(10))
                .statusRessarcimento(StatusRessarcimento.RESSARCIDO)
                .dataRessarcimento(LocalDate.now().minusDays(2))
                .build();

        DespesaProcessual saved = despesaRepository.saveAndFlush(despesa);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatusRessarcimento()).isEqualTo(StatusRessarcimento.RESSARCIDO);
        assertThat(saved.getDataRessarcimento()).isNotNull();
    }

    @Test
    void shouldFailWhenStatusIsRessarcidoAndDataRessarcimentoIsNull() {
        DespesaProcessual despesa = DespesaProcessual.builder()
                .tenant(sharedTenant)
                .processo(sharedProcesso)
                .descricao("Despesa Inválida")
                .tipo(TipoDespesa.DILIGENCIA)
                .valor(new BigDecimal("180.00"))
                .dataDespesa(LocalDate.now())
                .statusRessarcimento(StatusRessarcimento.RESSARCIDO)
                .dataRessarcimento(null) // Viola regra de banco de dados
                .build();

        assertThatThrownBy(() -> despesaRepository.saveAndFlush(despesa))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
