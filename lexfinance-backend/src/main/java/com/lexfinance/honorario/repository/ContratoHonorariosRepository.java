package com.lexfinance.honorario.repository;

import com.lexfinance.honorario.domain.ContratoHonorarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContratoHonorariosRepository extends JpaRepository<ContratoHonorarios, UUID> {
}
