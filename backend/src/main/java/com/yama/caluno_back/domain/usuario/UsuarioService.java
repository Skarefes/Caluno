package com.yama.caluno_back.domain.usuario;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //metodo que cadastra o usuario e identifica se o nome ja existe

    public void cadastrar(DadosCadastroUsuario dadosUsuario) throws IllegalAccessException {
        if (usuarioRepository.existsByNome(dadosUsuario.nome())){
            throw new IllegalAccessException("Nome do usuario ja existem, por gentileza cadastre outro");
        }
        //Caso o nome não exista, ele cria um novo usuario
        Usuario usuario = new Usuario(dadosUsuario);
        //Ele pega a senha do usuario e joga no Bcrypt para que a senha não fique visivel para qualquer um
        usuario.setSenha(passwordEncoder.encode(dadosUsuario.senha()));
        //Por fim salva o usuario
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(String nome){
        Usuario usuario = usuarioRepository.findByNome(nome).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado"));
        usuarioRepository.delete(usuario);
    }
}
