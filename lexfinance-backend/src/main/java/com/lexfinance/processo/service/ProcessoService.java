package com.lexfinance.processo.service;

import com.lexfinance.cliente.domain.Cliente;
import com.lexfinance.cliente.repository.ClienteRepository;
import com.lexfinance.config.TenantContext;
import com.lexfinance.processo.domain.Processo;
import com.lexfinance.processo.dto.ProcessoDTO;
import com.lexfinance.processo.repository.ProcessoRepository;
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
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<ProcessoDTO> findAll() {
        List<Processo> processos = processoRepository.findAll();
        return processos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProcessoDTO create(ProcessoDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.getReferenceById(tenantId);

        Cliente cliente = clienteRepository.findByIdAndTenantId(dto.clienteId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        Processo processo = Processo.builder()
                .tenant(tenant)
                .cliente(cliente)
                .numeroCnj(dto.numeroCnj())
                .descricao(dto.descricao())
                .area(dto.area())
                .status(dto.status())
                .dataInicio(dto.dataInicio())
                .build();

        Processo saved = processoRepository.save(processo);
        return toDTO(saved);
    }

    @Transactional
    public ProcessoDTO update(UUID id, ProcessoDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        Cliente cliente = clienteRepository.findByIdAndTenantId(dto.clienteId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        processo.setCliente(cliente);
        processo.setNumeroCnj(dto.numeroCnj());
        processo.setDescricao(dto.descricao());
        processo.setArea(dto.area());
        processo.setStatus(dto.status());
        processo.setDataInicio(dto.dataInicio());

        Processo saved = processoRepository.save(processo);
        return toDTO(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));
        processoRepository.delete(processo);
    }

    private ProcessoDTO toDTO(Processo processo) {
        return new ProcessoDTO(
                processo.getId(),
                processo.getCliente().getId(),
                processo.getCliente().getNome(),
                processo.getNumeroCnj(),
                processo.getDescricao(),
                processo.getArea(),
                processo.getStatus(),
                processo.getDataInicio()
        );
    }
}
