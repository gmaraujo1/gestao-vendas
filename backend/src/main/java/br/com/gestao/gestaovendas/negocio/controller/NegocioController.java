package br.com.gestao.gestaovendas.negocio.controller;

import br.com.gestao.gestaovendas.common.security.UsuarioLogado;
import br.com.gestao.gestaovendas.negocio.dto.NegocioResponseDTO;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/negocio")
public class NegocioController {

    private final NegocioRepository negocioRepository;

    /** Negócios que o usuário logado pode escolher no front. */
    @GetMapping("/meus")
    public List<NegocioResponseDTO> meusNegocios() {
        return negocioRepository.findAtivosDoUsuario(UsuarioLogado.id()).stream()
                .map(n -> new NegocioResponseDTO(n.getId(), n.getCodigo(), n.getNome()))
                .toList();
    }
}
