package br.com.minas.iluminacao.controller;

import br.com.minas.iluminacao.dto.DashboardResponse;
import br.com.minas.iluminacao.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping
    public DashboardResponse dashboard() { return service.montar(); }
}
