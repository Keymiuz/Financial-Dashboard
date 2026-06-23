package com.lexfinance.dashboard.dto;

import java.math.BigDecimal;

public record FluxoCaixaDTO(
        String data,
        BigDecimal totalEsperado
) {
}
