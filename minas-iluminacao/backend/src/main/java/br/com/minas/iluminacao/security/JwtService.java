package br.com.minas.iluminacao.security;

import br.com.minas.iluminacao.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-horas}") long horas) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = horas * 3600_000L;
    }

    public String gerar(Usuario u) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("perfil", u.getPerfil().name())
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    /** Lança JwtException se o token for inválido ou estiver expirado. */
    public Claims validar(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
    }
}
