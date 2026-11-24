package br.com.gestao.gestaovendas.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestEditDTO {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Email(message = "E-mail inválido")
    private String email;

    /** Opcional: quando vier, substitui os negócios do usuário. */
    @Size(min = 1, message = "Informe ao menos um negócio")
    private Set<@NotNull Long> negocioIds;

}
