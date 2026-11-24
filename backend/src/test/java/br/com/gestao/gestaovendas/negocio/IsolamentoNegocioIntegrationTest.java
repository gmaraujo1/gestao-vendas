package br.com.gestao.gestaovendas.negocio;

import br.com.gestao.gestaovendas.categoria.domain.Categoria;
import br.com.gestao.gestaovendas.categoria.repository.CategoriaRepository;
import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import br.com.gestao.gestaovendas.produto.repository.ProdutoRepository;
import br.com.gestao.gestaovendas.support.IntegrationTest;
import br.com.gestao.gestaovendas.support.LimpezaBanco;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class IsolamentoNegocioIntegrationTest {

    private static final String HEADER = "X-Negocio-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private NegocioRepository negocioRepository;
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LimpezaBanco limpezaBanco;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long laticinio;
    private Long paoNino;
    private String tokenAdmin;
    private String tokenFuncionario;

    @BeforeEach
    void setUp() throws Exception {
        limpezaBanco.apagarProdutosEHistorico();
        laticinio = idDo("LATICINIO");
        paoNino = idDo("PAO_NINO");
        garanteFuncionarioSoDoLaticinio();
        tokenAdmin = login("admin@teste.com", "senha-admin-teste");
        tokenFuncionario = login("func.laticinio@teste.com", "senha-func");
    }

    @Test
    void adminInicialVeOsDoisNegocios() throws Exception {
        mockMvc.perform(get("/api/negocio/meus").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo").value(org.hamcrest.Matchers.containsInAnyOrder("LATICINIO", "PAO_NINO")));
    }

    @Test
    void funcionarioVeSoOLaticinio() throws Exception {
        mockMvc.perform(get("/api/negocio/meus").header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo").value("LATICINIO"));
    }

    @Test
    void produtoDeUmNegocioNaoApareceNoOutro() throws Exception {
        cadastrarProduto(tokenAdmin, laticinio, 10, "Leite integral");

        mockMvc.perform(comNegocio(get("/api/produto"), tokenAdmin, paoNino))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(0));

        mockMvc.perform(comNegocio(get("/api/produto"), tokenAdmin, laticinio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].descricao").value("Leite integral"));
    }

    @Test
    void naoEditaNemRemoveProdutoDeOutroNegocio() throws Exception {
        Long id = cadastrarProduto(tokenAdmin, laticinio, 11, "Queijo minas");

        mockMvc.perform(comNegocio(patch("/api/produto/" + id), tokenAdmin, paoNino)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descricao": "Hackeado"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(comNegocio(delete("/api/produto/" + id), tokenAdmin, paoNino))
                .andExpect(status().isNotFound());
    }

    @Test
    void funcionarioSemVinculoRecebe403() throws Exception {
        mockMvc.perform(comNegocio(get("/api/produto"), tokenFuncionario, paoNino))
                .andExpect(status().isForbidden());
    }

    @Test
    void semHeaderDeNegocioRecebe400() throws Exception {
        mockMvc.perform(get("/api/produto").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Informe o negócio no header X-Negocio-Id"));
    }

    @Test
    void mesmoCodigoPodeExistirEmNegociosDiferentes() throws Exception {
        cadastrarProduto(tokenAdmin, laticinio, 20, "Iogurte");
        cadastrarProduto(tokenAdmin, paoNino, 20, "Pão francês");

        mockMvc.perform(comNegocio(post("/api/produto"), tokenAdmin, laticinio)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson(laticinio, 20, "Repetido")))
                .andExpect(status().isConflict());
    }

    private Long cadastrarProduto(String token, Long negocioId, int codigo, String descricao) throws Exception {
        String body = mockMvc.perform(comNegocio(post("/api/produto"), token, negocioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(produtoJson(negocioId, codigo, descricao)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    /** Usa a primeira categoria do negócio (criando "Geral" se não houver). */
    private String produtoJson(Long negocioId, int codigo, String descricao) {
        return """
                {"codigo": %d, "descricao": "%s", "categoriaId": %d,
                 "unidadeMedida": "UN", "precoMedio": 4.50}
                """.formatted(codigo, descricao, categoriaDo(negocioId));
    }

    private Long categoriaDo(Long negocioId) {
        return categoriaRepository.findAllByNegocioIdOrderByNomeAsc(negocioId).stream().findFirst()
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNegocioId(negocioId);
                    c.setNome("Geral");
                    return categoriaRepository.save(c);
                }).getId();
    }

    private static MockHttpServletRequestBuilder comNegocio(MockHttpServletRequestBuilder req, String token, Long negocioId) {
        return req.header("Authorization", "Bearer " + token).header(HEADER, negocioId);
    }

    private void garanteFuncionarioSoDoLaticinio() {
        if (usuarioRepository.findByEmail("func.laticinio@teste.com").isPresent()) {
            return;
        }
        Usuario u = new Usuario();
        u.setNome("Funcionário Laticínio");
        u.setEmail("func.laticinio@teste.com");
        u.setSenha(passwordEncoder.encode("senha-func"));
        u.setTipoUsuario(TipoUsuario.FUNCIONARIO);
        u.setNegocios(new HashSet<>(List.of(negocioRepository.getReferenceById(laticinio))));
        u.setCriadoEm(LocalDateTime.now());
        u.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.save(u);
    }

    private Long idDo(String codigo) {
        return negocioRepository.findAll().stream()
                .filter(n -> n.getCodigo().equals(codigo))
                .map(Negocio::getId)
                .findFirst().orElseThrow();
    }

    private String login(String email, String senha) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
