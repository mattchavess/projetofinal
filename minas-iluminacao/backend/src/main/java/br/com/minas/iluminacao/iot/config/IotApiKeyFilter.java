package br.com.minas.iluminacao.iot.config;

import br.com.minas.iluminacao.exception.ErroResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** Exige o cabeçalho X-API-KEY nas rotas /api/iot/** (dispositivos não usam login de operador). */
@Component
public class IotApiKeyFilter extends OncePerRequestFilter {

    private final String chave;
    private final ObjectMapper objectMapper;

    public IotApiKeyFilter(@Value("${app.iot.api-key}") String chave, ObjectMapper objectMapper) {
        this.chave = chave;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/iot/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String enviada = request.getHeader("X-API-KEY");
        boolean valida = enviada != null && MessageDigest.isEqual(
                enviada.getBytes(StandardCharsets.UTF_8), chave.getBytes(StandardCharsets.UTF_8));
        if (!valida) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    ErroResposta.de(401, "Não autenticado", "Chave de API do dispositivo ausente ou inválida"));
            return;
        }
        chain.doFilter(request, response);
    }
}
