package br.com.gestao.gestaogastos.produto.domain;

import br.com.gestao.gestaogastos.exception.ConflictException;
import br.com.gestao.gestaogastos.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProdutoValidator {

    private final ProdutoRepository produtoRepository;

    public void validaCodigoProduto(Integer codigo) {
        Boolean codigoExistente = produtoRepository.existsByCodigo(codigo);
        if (codigoExistente) {
            throw new ConflictException("Código de produto já existente!");
        }
    }
}
