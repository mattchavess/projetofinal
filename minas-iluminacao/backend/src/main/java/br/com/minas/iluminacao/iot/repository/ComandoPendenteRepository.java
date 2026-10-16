package br.com.minas.iluminacao.iot.repository;

import br.com.minas.iluminacao.iot.model.ComandoPendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComandoPendenteRepository extends JpaRepository<ComandoPendente, Long> {

    Optional<ComandoPendente> findFirstByTorreIdAndEntregueFalseOrderByCriadoEmAsc(Long torreId);

    boolean existsByTorreIdAndEntregueFalse(Long torreId);

    void deleteByTorreId(Long torreId);
}
