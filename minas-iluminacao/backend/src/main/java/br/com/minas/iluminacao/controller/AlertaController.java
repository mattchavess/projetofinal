package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.AlertaRequest;
import br.com.minas.iluminacao.dto.AlertaResponse;
import br.com.minas.iluminacao.model.enums.NivelAlerta;
import br.com.minas.iluminacao.model.enums.StatusAlerta;
import br.com.minas.iluminacao.service.AlertaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService service;

    @GetMapping
    public List<AlertaResponse> listar(@RequestParam(required = false) StatusAlerta status,
                                       @RequestParam(required = false) NivelAlerta nivel,
                                       @RequestParam(required = false) Long torreId) {
        return service.listar(status, nivel, torreId);
    }

    @PostMapping
    public ResponseEntity<AlertaResponse> criar(@Valid @RequestBody AlertaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PatchMapping("/{id}/resolver")
    public AlertaResponse resolver(@PathVariable Long id) { return service.resolver(id); }
}
