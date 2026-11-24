package br.com.gestao.gestaovendas.usuario.domain;

import br.com.gestao.gestaovendas.exception.ResourceNotFoundException;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VinculoNegocioService {

    private final NegocioRepository negocioRepository;

    /** Substitui os negócios do usuário; falha se algum id não existir. */
    public void definirNegocios(Usuario usuario, Set<Long> negocioIds) {
        List<Negocio> negocios = negocioRepository.findAllById(negocioIds);
        if (negocios.size() != negocioIds.size()) {
            throw new ResourceNotFoundException("Negócio não encontrado");
        }
        usuario.setNegocios(new HashSet<>(negocios));
    }
}
