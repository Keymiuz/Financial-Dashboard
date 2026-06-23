package com.lexfinance.dashboard.dto;

import java.math.BigDecimal;

public record ResumoDTO(
        BigDecimal aReceber,
        BigDecimal emAtraso,
        BigDecimal recebido,
        long qtdAtrasadas
) {
}
