package com.lexfinance.processo.controller;

import com.lexfinance.processo.dto.ProcessoDTO;
import com.lexfinance.processo.service.ProcessoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/processos")
@RequiredArgsConstructor
public class ProcessoController {

    private final ProcessoService processoService;

    @GetMapping
    public ResponseEntity<List<ProcessoDTO>> getAll() {
        return ResponseEntity.ok(processoService.findAll());
    }

    @PostMapping
    public ResponseEntity<ProcessoDTO> create(@Valid @RequestBody ProcessoDTO dto) {
        return ResponseEntity.ok(processoService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoDTO> update(@PathVariable UUID id, @Valid @RequestBody ProcessoDTO dto) {
        return ResponseEntity.ok(processoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        processoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
