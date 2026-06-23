package com.lexfinance.dashboard.controller;

import com.lexfinance.dashboard.dto.FluxoCaixaDTO;
import com.lexfinance.dashboard.dto.ResumoDTO;
import com.lexfinance.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    public ResponseEntity<ResumoDTO> getResumo() {
        return ResponseEntity.ok(dashboardService.getResumo());
    }

    @GetMapping("/fluxo-caixa")
    public ResponseEntity<List<FluxoCaixaDTO>> getFluxoCaixa() {
        return ResponseEntity.ok(dashboardService.getFluxoCaixa());
    }
}
