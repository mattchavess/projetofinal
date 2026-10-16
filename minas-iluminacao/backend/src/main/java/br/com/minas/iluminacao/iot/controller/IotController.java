package br.com.minas.iluminacao.iot.controller;

import br.com.minas.iluminacao.iot.dto.ComandoIotResponse;
import br.com.minas.iluminacao.iot.dto.TelemetriaRequest;
import br.com.minas.iluminacao.iot.dto.TelemetriaResponse;
import br.com.minas.iluminacao.iot.service.ComandoPendenteService;
import br.com.minas.iluminacao.iot.service.TelemetriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
public class IotController {

    private final TelemetriaService telemetriaService;
    private final ComandoPendenteService comandoService;

    /** O ESP32 envia luminosidade, bateria e estado; a resposta indica se há comando esperando. */
    @PostMapping("/telemetria")
    public TelemetriaResponse telemetria(@Valid @RequestBody TelemetriaRequest req) {
        return telemetriaService.registrar(req);
    }

    /** Polling: 200 com o próximo comando, ou 204 se não houver nenhum. */
    @GetMapping("/torres/{id}/comando")
    public ResponseEntity<ComandoIotResponse> comando(@PathVariable Long id) {
        return comandoService.proximoComando(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
