package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.HistoricoOperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface HistoricoOperacaoRepository extends JpaRepository<HistoricoOperacao, Long> {

    List<HistoricoOperacao> findTop200ByOrderByDataHoraDesc();

    List<HistoricoOperacao> findTop200ByTorreIdOrderByDataHoraDesc(Long torreId);

    @Modifying
    @Query("update HistoricoOperacao h set h.torre = null where h.torre.id = :torreId")
    void desvincularTorre(@Param("torreId") Long torreId);
}
