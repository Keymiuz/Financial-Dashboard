package com.lexfinance.honorario.dto;

import com.lexfinance.honorario.domain.StatusParcela;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ParcelaHonorariosDTO(
        UUID id,

        @NotNull(message = "O contrato é obrigatório")
        UUID contratoId,

        Integer numeroParcela,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dataVencimento,

        StatusParcela status,
        LocalDate dataRecebimento,
        String observacao
) {
}
