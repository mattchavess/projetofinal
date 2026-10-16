package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.*;
import br.com.minas.iluminacao.model.enums.StatusTorre;
import br.com.minas.iluminacao.service.TorreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torres")
@RequiredArgsConstructor
public class TorreController {

    private final TorreService service;

    @GetMapping
    public List<TorreResponse> listar(@RequestParam(required = false) String busca,
                                      @RequestParam(required = false) StatusTorre status,
                                      @RequestParam(required = false) Long setorId) {
        return service.listar(busca, status, setorId);
    }

    @GetMapping("/{id}")
    public TorreResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<TorreResponse> criar(@Valid @RequestBody TorreRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @PutMapping("/{id}")
    public TorreResponse atualizar(@PathVariable Long id, @Valid @RequestBody TorreRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public TorreResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(id, req.status());
    }

    @PatchMapping("/{id}/modo")
    public TorreResponse alterarModo(@PathVariable Long id, @Valid @RequestBody ModoRequest req) {
        return service.alterarModo(id, req.modo());
    }

    /** Comando individual: LIGAR, DESLIGAR ou um dos modos de iluminação. */
    @PostMapping("/{id}/comando")
    public TorreResponse comando(@PathVariable Long id, @Valid @RequestBody ComandoRequest req) {
        return service.executarComando(id, req.comando());
    }

    @PostMapping("/comando-grupo")
    public ComandoGrupoResponse comandoGrupo(@Valid @RequestBody ComandoGrupoRequest req) {
        return service.executarComandoGrupo(req);
    }
}
