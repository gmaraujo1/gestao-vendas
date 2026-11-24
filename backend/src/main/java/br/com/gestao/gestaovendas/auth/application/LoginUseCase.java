package br.com.gestao.gestaovendas.auth.application;

import br.com.gestao.gestaovendas.auth.dto.LoginRequestDTO;
import br.com.gestao.gestaovendas.auth.dto.LoginResponseDTO;
import br.com.gestao.gestaovendas.common.text.Emails;
import br.com.gestao.gestaovendas.config.JwtProperties;
import br.com.gestao.gestaovendas.config.SecurityConfig;
import br.com.gestao.gestaovendas.usuario.domain.Usuario;
import br.com.gestao.gestaovendas.usuario.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    /**
     * Hash de uma senha aleatória, comparado quando o e-mail não existe: o BCrypt roda
     * nos dois casos e o tempo de resposta não revela quem tem conta.
     */
    private String hashFalso;

    @PostConstruct
    void gerarHashFalso() {
        hashFalso = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail(Emails.normalizar(request.email()));
        String hash = encontrado.map(Usuario::getSenha).orElse(hashFalso);
        boolean senhaConfere = passwordEncoder.matches(request.senha(), hash);
        Usuario usuario = encontrado
                .filter(u -> senhaConfere)
                // mesma mensagem para e-mail inexistente e senha errada
                .orElseThrow(() -> new BadCredentialsException(MENSAGEM_CREDENCIAIS_INVALIDAS));

        Instant agora = clock.instant();
        Instant expiraEm = agora.plus(jwtProperties.expiracao());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .claim("nome", usuario.getNome())
                .claim(SecurityConfig.CLAIM_TIPO, List.of(usuario.getTipoUsuario().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponseDTO(token, expiraEm, usuario.getId(), usuario.getNome(), usuario.getTipoUsuario());
    }
}
