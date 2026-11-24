package br.com.gestao.gestaovendas.support;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de API: login, ids dos negócios e helpers de requisição.
 * O ADMIN inicial (application-test.yaml) acessa os dois negócios.
 */
@IntegrationTest
public abstract class ApiIntegrationTest {

    protected static final String SENHA_ADMIN = "senha-admin-teste";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UsuarioRepository usuarioRepository;
    @Autowired
    protected NegocioRepository negocioRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Long laticinio() {
        return idDoNegocio("LATICINIO");
    }

    protected Long paoNino() {
        return idDoNegocio("PAO_NINO");
    }

    protected String tokenAdmin() throws Exception {
        return login("admin@teste.com", SENHA_ADMIN);
    }

    /** Cria um FUNCIONARIO novo, vinculado só aos negócios informados, e devolve o token dele. */
    protected String tokenFuncionario(Long... negocioIds) throws Exception {
        String email = "func." + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        Usuario u = new Usuario();
        u.setNome("Funcionário " + email);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode("senha-func-123"));
        u.setTipoUsuario(TipoUsuario.FUNCIONARIO);
        u.setNegocios(new HashSet<>(negocioRepository.findAllById(Set.of(negocioIds))));
        u.setCriadoEm(java.time.LocalDateTime.now());
        u.setAtualizadoEm(java.time.LocalDateTime.now());
        usuarioRepository.save(u);
        return login(email, "senha-func-123");
    }

    protected String login(String email, String senha) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    protected static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token);
    }

    protected static MockHttpServletRequestBuilder noNegocio(MockHttpServletRequestBuilder req, String token, Long negocioId) {
        return comToken(req, token).header("X-Negocio-Id", negocioId);
    }

    protected static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String body) {
        return req.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    protected static Long id(String jsonBody) {
        return ((Number) JsonPath.read(jsonBody, "$.id")).longValue();
    }

    protected static String sufixo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long idDoNegocio(String codigo) {
        return negocioRepository.findAll().stream()
                .filter(n -> n.getCodigo().equals(codigo))
                .map(Negocio::getId)
                .findFirst().orElseThrow();
    }
}
