package br.com.gestao.gestaovendas.categoria.application;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaRequestDTO;
import br.com.gestao.gestaovendas.categoria.dto.CategoriaResponseDTO;
import br.com.gestao.gestaovendas.categoria.mapper.CategoriaMapper;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastrarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional
    public CategoriaResponseDTO cadastrar(Long negocioId, CategoriaRequestDTO request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNegocioIdAndNomeIgnoreCase(negocioId, nome)) {
            throw new ConflictException("Já existe uma categoria com esse nome");
        }
        Categoria categoria = new Categoria();
        categoria.setNegocioId(negocioId);
        categoria.setNome(nome);
        return categoriaMapper.toResponse(categoriaRepository.save(categoria));
    }
}
