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
                dados.proteinas(),
                dados.carboidratos(),
                dados.gorduras(),
                dados.sodio(),
                dados.fibras()
        );
    }

    //verifica as informações recebida, e  só altera os campos se o novo valor não for null
    public void atualizarInformacaoNutricional(DadosAtualizacaoAlimento dados){
        if (dados.nome() != null ){
            this.nome = dados.nome();
        }
        if (dados.calorias() != null){
            this.informacaoNutricional.setCalorias(dados.calorias());
        }
        if (dados.proteinas() != null){
            this.informacaoNutricional.setProteinas(dados.proteinas());
        }
        if (dados.carboidratos() != null){
            this.informacaoNutricional.setCarboidratos(dados.carboidratos());
        }
        if (dados.gorduras() != null){
            this.informacaoNutricional.setGorduras(dados.gorduras());
        }
        if (dados.sodio() != null){
            this.informacaoNutricional.setSodio(dados.sodio());
        }
        if (dados.fibras() != null){
            this.informacaoNutricional.setFibras(dados.fibras());
        }
    }

}
