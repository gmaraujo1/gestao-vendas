package br.com.gestao.gestaovendas.auth;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.support.IntegrationTest;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void garanteFuncionario() {
        if (usuarioRepository.findByEmail("func@teste.com").isEmpty()) {
            Usuario u = new Usuario();
            u.setNome("Funcionário");
            u.setEmail("func@teste.com");
            u.setSenha(passwordEncoder.encode("senha-func"));
            u.setTipoUsuario(TipoUsuario.FUNCIONARIO);
            u.setCriadoEm(LocalDateTime.now());
            u.setAtualizadoEm(LocalDateTime.now());
            usuarioRepository.save(u);
        }
    }

    @Test
    void adminInicialEhCriadoComSenhaCriptografada() {
        Usuario admin = usuarioRepository.findByEmail("admin@teste.com").orElseThrow();

        assertThat(admin.getTipoUsuario()).isEqualTo(TipoUsuario.ADMIN);
        assertThat(admin.getSenha()).isNotEqualTo("senha-admin-teste").startsWith("$2");
    }

    @Test
    void adminLogaEAcessaUsuarios() throws Exception {
        String token = login("admin@teste.com", "senha-admin-teste");

        mockMvc.perform(get("/api/usuario/listar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void funcionarioNaoAcessaUsuarios() throws Exception {
        String token = login("func@teste.com", "senha-func");

        mockMvc.perform(get("/api/usuario/listar").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void senhaErradaRetorna401ComMensagemGenerica() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@teste.com", "senha": "errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void emailInexistenteRetornaMesmaMensagem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "ninguem@teste.com", "senha": "qualquer"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/produto")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {
        String token = login("func@teste.com", "senha-func");
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        mockMvc.perform(get("/api/produto").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    private String login(String email, String senha) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"%s\", \"senha\": \"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
