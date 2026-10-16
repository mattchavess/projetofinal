package br.com.minas.iluminacao.dto;

import java.util.Map;

public record DashboardResponse(
        long totalTorres,
        long ligadas,
        long desligadas,
        long emManutencao,
        long comAlerta,
        long bateriaCritica,
        double consumoEstimadoKw,
        double horasAtividadeTotal,
        double disponibilidadePercentual,
        Map<String, Long> torresPorStatus,
        Map<String, Long> alertasPorNivel,
        Map<String, Long> alertasPorTipo,
        Map<String, Double> consumoPorSetorKw
) {}
