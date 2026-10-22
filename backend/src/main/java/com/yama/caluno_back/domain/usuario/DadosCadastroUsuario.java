package com.yama.caluno_back.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank String nome,
        @NotBlank @Size(min = 6) String senha // a senha tem que ter no minimo 6 digitos
) {
}
