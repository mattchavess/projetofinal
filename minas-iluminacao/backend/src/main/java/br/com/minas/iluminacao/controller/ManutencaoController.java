package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.ManutencaoRequest;
import br.com.minas.iluminacao.dto.ManutencaoResponse;
import br.com.minas.iluminacao.model.enums.StatusManutencao;
import br.com.minas.iluminacao.service.ManutencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manutencoes")
@RequiredArgsConstructor
public class ManutencaoController {

    private final ManutencaoService service;

    @GetMapping
    public List<ManutencaoResponse> listar(@RequestParam(required = false) StatusManutencao status,
                                           @RequestParam(required = false) Long torreId) {
        return service.listar(status, torreId);
    }

    @GetMapping("/{id}")
    public ManutencaoResponse buscar(@PathVariable Long id) { return service.buscar(id); }

    @PostMapping
    public ResponseEntity<ManutencaoResponse> criar(@Valid @RequestBody ManutencaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public ManutencaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ManutencaoRequest req) {
        return service.atualizar(id, req);
    }
}
