package com.lexfinance.cliente.controller;

import com.lexfinance.cliente.dto.ClienteDTO;
import com.lexfinance.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAll() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> create(@Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(@PathVariable UUID id, @Valid @RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto));
    }
}
