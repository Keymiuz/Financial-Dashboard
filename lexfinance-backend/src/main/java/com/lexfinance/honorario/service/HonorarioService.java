package com.lexfinance.honorario.service;

import com.lexfinance.config.TenantContext;
import com.lexfinance.honorario.domain.ContratoHonorarios;
import com.lexfinance.honorario.domain.ParcelaHonorarios;
import com.lexfinance.honorario.domain.StatusParcela;
import com.lexfinance.honorario.dto.ContratoHonorariosDTO;
import com.lexfinance.honorario.dto.ParcelaHonorariosDTO;
import com.lexfinance.honorario.repository.ContratoHonorariosRepository;
import com.lexfinance.honorario.repository.ParcelaHonorariosRepository;
import com.lexfinance.processo.domain.Processo;
import com.lexfinance.processo.repository.ProcessoRepository;
import com.lexfinance.tenant.domain.Tenant;
import com.lexfinance.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HonorarioService {

    private final ContratoHonorariosRepository contratoRepository;
    private final ParcelaHonorariosRepository parcelaRepository;
    private final ProcessoRepository processoRepository;
    private final TenantRepository tenantRepository;

    // ─── Contratos ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContratoHonorariosDTO> findAllContratos() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return contratoRepository.findAllByTenantId(tenantId).stream()
                .map(this::toContratoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContratoHonorariosDTO createContrato(ContratoHonorariosDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.getReferenceById(tenantId);
        Processo processo = processoRepository.findById(dto.processoId())
                .orElseThrow(() -> new IllegalArgumentException("Processo não encontrado"));

        ContratoHonorarios contrato = ContratoHonorarios.builder()
                .tenant(tenant)
                .processo(processo)
                .tipo(dto.tipo())
                .valorFixo(dto.valorFixo())
                .valorHora(dto.valorHora())
                .descricao(dto.descricao())
                .dataContrato(dto.dataContrato())
                .build();

        return toContratoDTO(contratoRepository.save(contrato));
    }

    // ─── Parcelas ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ParcelaHonorariosDTO> findParcelasByContrato(UUID contratoId) {
        return parcelaRepository.findByContratoId(contratoId).stream()
                .map(this::toParcelaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParcelaHonorariosDTO createParcela(ParcelaHonorariosDTO dto) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.getReferenceById(tenantId);
        ContratoHonorarios contrato = contratoRepository.findById(dto.contratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));

        // Auto-increment numero_parcela
        List<ParcelaHonorarios> existentes = parcelaRepository.findByContratoId(dto.contratoId());
        int numeroParcela = existentes.size() + 1;

        ParcelaHonorarios parcela = ParcelaHonorarios.builder()
                .tenant(tenant)
                .contrato(contrato)
                .numeroParcela(numeroParcela)
                .valor(dto.valor())
                .dataVencimento(dto.dataVencimento())
                .status(StatusParcela.PENDENTE)
                .observacao(dto.observacao())
                .build();

        return toParcelaDTO(parcelaRepository.save(parcela));
    }

    @Transactional
    public ParcelaHonorariosDTO marcarRecebido(UUID parcelaId, LocalDate dataRecebimento) {
        ParcelaHonorarios parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new IllegalArgumentException("Parcela não encontrada"));
        parcela.setStatus(StatusParcela.RECEBIDO);
        parcela.setDataRecebimento(dataRecebimento != null ? dataRecebimento : LocalDate.now());
        return toParcelaDTO(parcelaRepository.save(parcela));
    }

    @Transactional
    public ParcelaHonorariosDTO marcarPendente(UUID parcelaId) {
        ParcelaHonorarios parcela = parcelaRepository.findById(parcelaId)
                .orElseThrow(() -> new IllegalArgumentException("Parcela não encontrada"));
        parcela.setStatus(StatusParcela.PENDENTE);
        parcela.setDataRecebimento(null);
        return toParcelaDTO(parcelaRepository.save(parcela));
    }

    // ─── Mappings ───────────────────────────────────────────────────────────

    private ContratoHonorariosDTO toContratoDTO(ContratoHonorarios c) {
        List<ParcelaHonorarios> parcelas = parcelaRepository.findByContratoId(c.getId());

        BigDecimal totalContratado = parcelas.stream()
                .map(ParcelaHonorarios::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecebido = parcelas.stream()
                .filter(p -> p.getStatus() == StatusParcela.RECEBIDO)
                .map(ParcelaHonorarios::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendente = parcelas.stream()
                .filter(p -> p.getStatus() == StatusParcela.PENDENTE)
                .map(ParcelaHonorarios::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ContratoHonorariosDTO(
                c.getId(),
                c.getProcesso().getId(),
                c.getProcesso().getNumeroCnj(),
                c.getProcesso().getCliente().getNome(),
                c.getTipo(),
                c.getValorFixo(),
                c.getValorHora(),
                c.getDescricao(),
                c.getDataContrato(),
                totalContratado,
                totalRecebido,
                totalPendente
        );
    }

    private ParcelaHonorariosDTO toParcelaDTO(ParcelaHonorarios p) {
        return new ParcelaHonorariosDTO(
                p.getId(),
                p.getContrato().getId(),
                p.getNumeroParcela(),
                p.getValor(),
                p.getDataVencimento(),
                p.getStatus(),
                p.getDataRecebimento(),
                p.getObservacao()
        );
    }
}
