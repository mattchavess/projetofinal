package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.Alerta;
import br.com.minas.iluminacao.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    @Query("""
            select a from Alerta a
            where (:status is null or a.status = :status)
              and (:nivel is null or a.nivel = :nivel)
              and (:torreId is null or a.torre.id = :torreId)
            order by a.dataHora desc
            """)
    List<Alerta> pesquisar(@Param("status") StatusAlerta status,
                           @Param("nivel") NivelAlerta nivel,
                           @Param("torreId") Long torreId);

    boolean existsByTorreIdAndTipoAndStatus(Long torreId, TipoAlerta tipo, StatusAlerta status);

    List<Alerta> findByTorreIdAndTipoAndStatus(Long torreId, TipoAlerta tipo, StatusAlerta status);

    long countByStatus(StatusAlerta status);

    long countByStatusAndNivel(StatusAlerta status, NivelAlerta nivel);

    long countByStatusAndTipo(StatusAlerta status, TipoAlerta tipo);

    @Query("select count(distinct a.torre.id) from Alerta a where a.status = :status")
    long contarTorresComAlerta(@Param("status") StatusAlerta status);

    void deleteByTorreId(Long torreId);
}
