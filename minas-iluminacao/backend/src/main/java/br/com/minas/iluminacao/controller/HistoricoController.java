package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.HistoricoResponse;
import br.com.minas.iluminacao.service.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historico")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoService service;

    @GetMapping
    public List<HistoricoResponse> listar(@RequestParam(required = false) Long torreId) {
        return service.listar(torreId);
    }
}
