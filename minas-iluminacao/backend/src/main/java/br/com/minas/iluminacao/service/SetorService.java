package br.com.minas.iluminacao.service;

import br.com.minas.iluminacao.dto.SetorResponse;
import br.com.minas.iluminacao.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetorService {

    private final SetorRepository repository;

    @Transactional(readOnly = true)
    public List<SetorResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(SetorResponse::de).toList();
    }
}
