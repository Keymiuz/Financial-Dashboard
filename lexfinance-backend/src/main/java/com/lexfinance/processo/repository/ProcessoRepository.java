package com.lexfinance.processo.repository;

import com.lexfinance.processo.domain.Processo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {
}
