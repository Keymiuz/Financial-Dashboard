package com.lexfinance.processo.dto;

import com.lexfinance.processo.domain.AreaProcesso;
import com.lexfinance.processo.domain.StatusProcesso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record ProcessoDTO(
        UUID id,
        
        @NotNull(message = "O cliente é obrigatório")
        UUID clienteId,
        
        String clienteNome,
        
        @NotBlank(message = "O número CNJ é obrigatório")
        @Size(max = 25, message = "O número CNJ deve ter no máximo 25 caracteres")
        String numeroCnj,
        
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,
        
        @NotNull(message = "A área do direito é obrigatória")
        AreaProcesso area,
        
        @NotNull(message = "O status do processo é obrigatório")
        StatusProcesso status,
        
        @NotNull(message = "A data de início é obrigatória")
        LocalDate dataInicio
) {
}
