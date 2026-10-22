package com.yama.caluno_back.domain.usuario;

import com.yama.caluno_back.domain.registro_consumo.RegistroConsumo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Table(name = "usuario")
@Entity
@NoArgsConstructor
public class Usuario {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column(nullable = false, unique = true)
   private String nome;
   @Column(nullable = false)
   private String senha;
   @OneToMany(mappedBy = "usuario")
   private List<RegistroConsumo> registroConsumos = new ArrayList<>();

   public Usuario(DadosCadastroUsuario dadosCadastroUsuario) {
      this.nome = dadosCadastroUsuario.nome();
      this.senha = dadosCadastroUsuario.senha();
   }

}
