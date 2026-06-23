package com.lexfinance.cliente.dto;

import com.lexfinance.cliente.domain.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClienteDTO(
        UUID id,
        
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
        String nome,
        
        @NotNull(message = "O tipo do cliente é obrigatório")
        TipoCliente tipo,
        
        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        @Size(max = 14, message = "O CPF/CNPJ deve ter no máximo 14 caracteres")
        String cpfCnpj,
        
        @Size(max = 200, message = "O e-mail deve ter no máximo 200 caracteres")
        String email,
        
        @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
        String telefone,
        
        boolean ativo
) {
}
