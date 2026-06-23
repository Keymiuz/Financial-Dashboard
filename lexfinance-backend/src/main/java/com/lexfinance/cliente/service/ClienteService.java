package com.lexfinance.cliente.service;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.dto.ClienteDTO;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.TenantContext;
import com.lexfinance.tenant.domain.Tenant;
import com.lexfinance.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<ClienteDTO> findAll() {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<Cliente> clientes = clienteRepository.findAllByTenantId(tenantId);
        return clientes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO create(ClienteDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.getReferenceById(tenantId);

        Cliente cliente = Cliente.builder()
                .tenant(tenant)
                .nome(dto.nome())
                .tipo(dto.tipo())
                .cpfCnpj(dto.cpfCnpj())
                .email(dto.email())
                .telefone(dto.telefone())
                .ativo(dto.ativo())
                .build();

        Cliente saved = clienteRepository.save(cliente);
        return toDTO(saved);
    }

    @Transactional
    public ClienteDTO update(UUID id, ClienteDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Cliente cliente = clienteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        cliente.setNome(dto.nome());
        cliente.setTipo(dto.tipo());
        cliente.setCpfCnpj(dto.cpfCnpj());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setAtivo(dto.ativo());

        Cliente saved = clienteRepository.save(cliente);
        return toDTO(saved);
    }

    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTipo(),
                cliente.getCpfCnpj(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.isAtivo()
        );
    }
}
