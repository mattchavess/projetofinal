package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.Torre;
import br.com.minas.iluminacao.model.enums.StatusTorre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TorreRepository extends JpaRepository<Torre, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    long countByStatus(StatusTorre status);

    long countByBateriaLessThan(Integer limite);

    /** Torres conectadas a um dispositivo (com leituras) cuja última comunicação é anterior ao limite. */
    @Query("""
            select t from Torre t
            where t.ultimaComunicacao < :limite
              and exists (select 1 from LeituraSensor l where l.torre = t)
            """)
    List<Torre> buscarSemComunicacao(@Param("limite") java.time.LocalDateTime limite);

    @Query("""
            select t from Torre t
            where (lower(t.nome) like lower(concat('%', :busca, '%'))
                   or lower(t.codigo) like lower(concat('%', :busca, '%')))
              and (:status is null or t.status = :status)
              and (:setorId is null or t.setor.id = :setorId)
            order by t.nome
            """)
    List<Torre> pesquisar(@Param("busca") String busca,
                          @Param("status") StatusTorre status,
                          @Param("setorId") Long setorId);
}
