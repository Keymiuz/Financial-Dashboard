package com.lexfinance.honorario.controller;

import com.lexfinance.honorario.dto.ContratoHonorariosDTO;
import com.lexfinance.honorario.dto.ParcelaHonorariosDTO;
import com.lexfinance.honorario.service.HonorarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/honorarios")
@RequiredArgsConstructor
public class HonorarioController {

    private final HonorarioService honorarioService;

    // ─── Contratos ──────────────────────────────────────────────────────────

    @GetMapping("/contratos")
    public ResponseEntity<List<ContratoHonorariosDTO>> getAllContratos() {
        return ResponseEntity.ok(honorarioService.findAllContratos());
    }

    @PostMapping("/contratos")
    public ResponseEntity<ContratoHonorariosDTO> createContrato(@Valid @RequestBody ContratoHonorariosDTO dto) {
        return ResponseEntity.ok(honorarioService.createContrato(dto));
    }

    // ─── Parcelas ───────────────────────────────────────────────────────────

    @GetMapping("/contratos/{contratoId}/parcelas")
    public ResponseEntity<List<ParcelaHonorariosDTO>> getParcelasByContrato(@PathVariable UUID contratoId) {
        return ResponseEntity.ok(honorarioService.findParcelasByContrato(contratoId));
    }

    @PostMapping("/parcelas")
    public ResponseEntity<ParcelaHonorariosDTO> createParcela(@Valid @RequestBody ParcelaHonorariosDTO dto) {
        return ResponseEntity.ok(honorarioService.createParcela(dto));
    }

    @PatchMapping("/parcelas/{id}/receber")
    public ResponseEntity<ParcelaHonorariosDTO> marcarRecebido(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        LocalDate dataRecebimento = null;
        if (body != null && body.containsKey("dataRecebimento")) {
            dataRecebimento = LocalDate.parse(body.get("dataRecebimento"));
        }
        return ResponseEntity.ok(honorarioService.marcarRecebido(id, dataRecebimento));
    }

    @PatchMapping("/parcelas/{id}/pendente")
    public ResponseEntity<ParcelaHonorariosDTO> marcarPendente(@PathVariable UUID id) {
        return ResponseEntity.ok(honorarioService.marcarPendente(id));
    }
}
