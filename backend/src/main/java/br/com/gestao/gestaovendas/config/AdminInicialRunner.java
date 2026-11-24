package br.com.gestao.gestaovendas.config;

import br.com.gestao.gestaovendas.common.enums.TipoUsuario;
import br.com.gestao.gestaovendas.common.text.Emails;
import br.com.gestao.gestaovendas.negocio.repository.NegocioRepository;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 * Sem usuário nenhum não há como logar (criar usuário exige ADMIN).
 * Na primeira subida, cria um ADMIN a partir de ADMIN_EMAIL/ADMIN_SENHA.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicialRunner implements ApplicationRunner {

    private final AdminInicialProperties props;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final NegocioRepository negocioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        if (!props.configurado()) {
            log.warn("Nenhum usuário cadastrado. Defina ADMIN_EMAIL e ADMIN_SENHA para criar o primeiro ADMIN.");
            return;
        }
        Usuario admin = new Usuario();
        admin.setNome(props.nome());
        admin.setEmail(Emails.normalizar(props.email()));
        admin.setSenha(passwordEncoder.encode(props.senha()));
        admin.setTipoUsuario(TipoUsuario.ADMIN);
        admin.setNegocios(new HashSet<>(negocioRepository.findAll()));
        admin.setCriadoEm(LocalDateTime.now());
        admin.setAtualizadoEm(LocalDateTime.now());
        usuarioRepository.save(admin);
        log.info("ADMIN inicial criado: {}", props.email());
    }
}
