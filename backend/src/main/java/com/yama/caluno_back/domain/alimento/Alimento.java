package com.yama.caluno_back.domain.alimento;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.dto.DadosAtualizacaoAlimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alimentos")
public class Alimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Embedded
    private InformacaoNutricional informacaoNutricional;

    public Alimento(DadosCadastroAlimento dados) {
        this.nome = dados.nome();
        this.informacaoNutricional = new InformacaoNutricional(
                dados.calorias(),
                dados.carboidratos(),
                dados.gorduras(),
                dados.sodio(),
                dados.fibras()
        );
    }

    public void atualizarInformacaoNutricional(DadosAtualizacaoAlimento dados){

    }

}
