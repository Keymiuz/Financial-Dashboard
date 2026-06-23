package com.lexfinance.tenant;

import com.lexfinance.tenant.domain.Tenant;
import com.lexfinance.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.context.annotation.Import;
import com.lexfinance.config.JpaAuditingConfig;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@Import(JpaAuditingConfig.class)
class TenantMappingTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void shouldPersistTenantWithGeneratedIdAndAuditInfo() {
        Tenant tenant = Tenant.builder()
                .nome("Escritório Teste TDD")
                .cnpj("11222333000199")
                .build();

        Tenant saved = tenantRepository.saveAndFlush(tenant);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isAtivo()).isTrue();
        assertThat(saved.getAuditInfo()).isNotNull();
        assertThat(saved.getAuditInfo().getCreatedAt()).isNotNull();
        assertThat(saved.getAuditInfo().getUpdatedAt()).isNotNull();
        // Permite tolerância de tempo para execução do teste
        assertThat(saved.getAuditInfo().getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldUpdateUpdatedAtOnModification() throws InterruptedException {
        Tenant tenant = Tenant.builder()
                .nome("Escritório Teste Update")
                .cnpj("44555666000188")
                .build();

        Tenant saved = tenantRepository.saveAndFlush(tenant);
        LocalDateTime initialCreatedAt = saved.getAuditInfo().getCreatedAt();
        LocalDateTime initialUpdatedAt = saved.getAuditInfo().getUpdatedAt();

        // Aguardar um instante pequeno para garantir diferença de timestamp no banco
        Thread.sleep(50);

        saved.setNome("Escritório Teste Alterado");
        Tenant updated = tenantRepository.saveAndFlush(saved);

        assertThat(updated.getAuditInfo().getCreatedAt()).isEqualTo(initialCreatedAt);
        assertThat(updated.getAuditInfo().getUpdatedAt()).isAfter(initialUpdatedAt);
    }

    @Test
    void shouldPerformSoftDeleteCorrectly() {
        Tenant tenant = Tenant.builder()
                .nome("Escritório Excluir")
                .cnpj("77888999000177")
                .build();

        Tenant saved = tenantRepository.saveAndFlush(tenant);
        UUID id = saved.getId();

        // Executar soft delete
        tenantRepository.delete(saved);
        tenantRepository.flush();

        // Buscar diretamente por findById (deve retornar vazio devido ao @SQLRestriction)
        Optional<Tenant> found = tenantRepository.findById(id);
        assertThat(found).isEmpty();
    }
}
