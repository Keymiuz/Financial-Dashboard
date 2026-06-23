package com.lexfinance.honorario.repository;

import com.lexfinance.honorario.domain.LancamentoHora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LancamentoHoraRepository extends JpaRepository<LancamentoHora, UUID> {
}
