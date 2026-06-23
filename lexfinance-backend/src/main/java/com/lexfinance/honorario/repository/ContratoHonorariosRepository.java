package com.lexfinance.honorario.repository;

import com.lexfinance.honorario.domain.ContratoHonorarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContratoHonorariosRepository extends JpaRepository<ContratoHonorarios, UUID> {

    @Query(value = "SELECT * FROM contratos_honorarios WHERE tenant_id = :tenantId AND ativo = true ORDER BY data_contrato DESC", nativeQuery = true)
    List<ContratoHonorarios> findAllByTenantId(@Param("tenantId") UUID tenantId);

    @Query(value = "SELECT * FROM contratos_honorarios WHERE processo_id = :processoId AND ativo = true ORDER BY data_contrato DESC", nativeQuery = true)
    List<ContratoHonorarios> findByProcessoId(@Param("processoId") UUID processoId);
}
