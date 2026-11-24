package br.com.gestao.gestaovendas.negocio.repository;

import br.com.gestao.gestaovendas.negocio.domain.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    @Query("""
            select count(n) > 0 from Usuario u join u.negocios n
            where u.id = :usuarioId and n.id = :negocioId and n.ativo = true
            """)
    boolean usuarioTemAcesso(@Param("usuarioId") Long usuarioId, @Param("negocioId") Long negocioId);

    @Query("""
            select n from Usuario u join u.negocios n
            where u.id = :usuarioId and n.ativo = true
            order by n.nome
            """)
    List<Negocio> findAtivosDoUsuario(@Param("usuarioId") Long usuarioId);
}
