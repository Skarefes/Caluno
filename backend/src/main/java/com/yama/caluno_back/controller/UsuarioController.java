package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.usuario.DadosCadastroUsuario;
import com.yama.caluno_back.domain.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Void> cadastrarUsuario(@RequestBody @Valid DadosCadastroUsuario dadosCadastroUsuario) throws IllegalAccessException {
        usuarioService.cadastrar(dadosCadastroUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
