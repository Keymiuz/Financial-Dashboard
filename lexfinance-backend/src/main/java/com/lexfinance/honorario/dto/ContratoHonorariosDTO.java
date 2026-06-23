package com.lexfinance.honorario.dto;

import com.lexfinance.honorario.domain.TipoContrato;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoHonorariosDTO(
        UUID id,

        @NotNull(message = "O processo é obrigatório")
        UUID processoId,

        String processoNumeroCnj,
        String clienteNome,

        @NotNull(message = "O tipo de contrato é obrigatório")
        TipoContrato tipo,

        BigDecimal valorFixo,
        BigDecimal valorHora,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotNull(message = "A data do contrato é obrigatória")
        LocalDate dataContrato,

        // Resumo financeiro calculado
        BigDecimal totalContratado,
        BigDecimal totalRecebido,
        BigDecimal totalPendente
) {
}
