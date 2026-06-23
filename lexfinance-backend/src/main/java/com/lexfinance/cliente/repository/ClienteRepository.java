package com.lexfinance.cliente.repository;

import com.lexfinance.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    @Query(value = "SELECT * FROM clientes WHERE tenant_id = ?1", nativeQuery = true)
    List<Cliente> findAllByTenantId(UUID tenantId);

    @Query(value = "SELECT * FROM clientes WHERE id = ?1 AND tenant_id = ?2", nativeQuery = true)
    Optional<Cliente> findByIdAndTenantId(UUID id, UUID tenantId);
}
