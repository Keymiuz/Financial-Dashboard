package com.lexfinance.dashboard.service;

import com.lexfinance.dashboard.dto.FluxoCaixaDTO;
import com.lexfinance.dashboard.dto.ResumoDTO;
import com.lexfinance.honorario.domain.ParcelaHonorarios;
import com.lexfinance.honorario.domain.StatusParcela;
import com.lexfinance.honorario.repository.ParcelaHonorariosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ParcelaHonorariosRepository repository;

    @Transactional(readOnly = true)
    public ResumoDTO getResumo() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        BigDecimal aReceber = repository.sumPendingByVencimentoBetween(startDate, endDate);
        BigDecimal emAtraso = repository.sumPendingByVencimentoBefore(today);
        BigDecimal recebido = repository.sumReceivedByRecebimentoBetween(startDate, endDate);
        long qtdAtrasadas = repository.countByStatusAndDataVencimentoBefore(StatusParcela.PENDENTE, today);

        return new ResumoDTO(aReceber, emAtraso, recebido, qtdAtrasadas);
    }

    @Transactional(readOnly = true)
    public List<FluxoCaixaDTO> getFluxoCaixa() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.plusDays(1);
        LocalDate endDate = today.plusDays(30);

        List<ParcelaHonorarios> parcelas = repository.findByStatusAndDataVencimentoBetween(
            StatusParcela.PENDENTE, startDate, endDate
        );

        Map<LocalDate, BigDecimal> valuesMap = parcelas.stream()
            .collect(Collectors.groupingBy(
                ParcelaHonorarios::getDataVencimento,
                Collectors.reducing(BigDecimal.ZERO, ParcelaHonorarios::getValor, BigDecimal::add)
            ));

        List<FluxoCaixaDTO> timeline = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            LocalDate date = today.plusDays(i);
            BigDecimal amount = valuesMap.getOrDefault(date, BigDecimal.ZERO);
            timeline.add(new FluxoCaixaDTO(date.toString(), amount));
        }

        return timeline;
    }
}
