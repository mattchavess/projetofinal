package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    List<LeituraSensor> findTop50ByTorreIdOrderByDataHoraDesc(Long torreId);

    boolean existsByTorreId(Long torreId);

    void deleteByTorreId(Long torreId);

    @Modifying
    @Query("delete from LeituraSensor l where l.dataHora < :limite")
    void apagarAnterioresA(@Param("limite") LocalDateTime limite);
}
