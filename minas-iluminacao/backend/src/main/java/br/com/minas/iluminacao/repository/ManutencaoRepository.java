package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.Manutencao;
import br.com.minas.iluminacao.model.enums.StatusManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    @Query("""
            select m from Manutencao m
            where (:status is null or m.status = :status)
              and (:torreId is null or m.torre.id = :torreId)
            order by m.dataAbertura desc
            """)
    List<Manutencao> pesquisar(@Param("status") StatusManutencao status,
                               @Param("torreId") Long torreId);

    void deleteByTorreId(Long torreId);
}
