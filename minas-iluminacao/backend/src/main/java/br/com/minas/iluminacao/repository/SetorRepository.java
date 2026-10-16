package br.com.minas.iluminacao.repository;

import br.com.minas.iluminacao.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findAllByOrderByNomeAsc();
}
