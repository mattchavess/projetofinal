package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.SetorResponse;
import br.com.minas.iluminacao.service.SetorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/setores")
@RequiredArgsConstructor
public class SetorController {

    private final SetorService service;

    @GetMapping
    public List<SetorResponse> listar() { return service.listar(); }
}
