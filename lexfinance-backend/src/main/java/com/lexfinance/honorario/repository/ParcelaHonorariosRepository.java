package com.lexfinance.honorario.repository;

import com.lexfinance.honorario.domain.ParcelaHonorarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lexfinance.honorario.domain.StatusParcela;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ParcelaHonorariosRepository extends JpaRepository<ParcelaHonorarios, UUID> {

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM ParcelaHonorarios p " +
           "WHERE p.status = 'PENDENTE' " +
           "AND p.dataVencimento >= :startDate AND p.dataVencimento <= :endDate")
    BigDecimal sumPendingByVencimentoBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM ParcelaHonorarios p " +
           "WHERE p.status = 'PENDENTE' " +
           "AND p.dataVencimento < :date")
    BigDecimal sumPendingByVencimentoBefore(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM ParcelaHonorarios p " +
           "WHERE p.status = 'RECEBIDO' " +
           "AND p.dataRecebimento >= :startDate AND p.dataRecebimento <= :endDate")
    BigDecimal sumReceivedByRecebimentoBetween(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    List<ParcelaHonorarios> findByStatusAndDataVencimentoBetween(
        StatusParcela status,
        LocalDate startDate,
        LocalDate endDate
    );

    long countByStatusAndDataVencimentoBefore(StatusParcela status, LocalDate date);

    @Query(value = "SELECT * FROM parcelas_honorarios WHERE contrato_id = :contratoId ORDER BY numero_parcela", nativeQuery = true)
    List<ParcelaHonorarios> findByContratoId(@Param("contratoId") UUID contratoId);
}
