package com.yama.caluno_back.infra.security;

import com.yama.caluno_back.domain.usuario.Usuario;
import com.yama.caluno_back.domain.usuario.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {
    //Esse service ele que faz criar usuarios no sistema

    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String nome) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByNome(nome). orElseThrow(() -> new UsernameNotFoundException("Nome de usuario não encontrado!"));

        return User.withUsername(usuario.getNome())
                .password(usuario.getSenha())
                .roles("USER")
                .build();
    }
}
