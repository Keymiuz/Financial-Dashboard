package com.lexfinance.despesa.repository;

import com.lexfinance.despesa.domain.DespesaProcessual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DespesaProcessualRepository extends JpaRepository<DespesaProcessual, UUID> {
}
