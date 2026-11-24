package br.com.gestao.gestaogastos.usuario.repository;

import br.com.gestao.gestaogastos.common.enums.TipoUsuario;
import br.com.gestao.gestaogastos.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByNome(String nome);

    List<Usuario> findByTipoUsuario(TipoUsuario tipoUsuario);

}
