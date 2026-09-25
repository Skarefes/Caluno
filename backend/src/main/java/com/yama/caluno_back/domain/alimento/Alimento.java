package com.yama.caluno_back.domain.alimento;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.registro_consumo.RegistroConsumo;
import com.yama.caluno_back.domain.dto.DadosAtualizacaoAlimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.infra.api.dto.DadosResultadoUSDA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alimentos")
public class Alimento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fdcId;
    private String nome;
    @Embedded
    private InformacaoNutricional informacaoNutricional;
    @OneToMany(mappedBy = "alimento")
    private List<RegistroConsumo> registroConsumos;


    public Alimento(DadosCadastroAlimento dados) {
        this.nome = dados.nome();
        //Os dados da nutrição está em um DTO
        this.informacaoNutricional = new InformacaoNutricional(
                dados.calorias(),
                dados.proteinas(),
                dados.carboidratos(),
                dados.gorduras(),
                dados.sodio(),
                dados.fibras()
        );
    }

    //Construtor que recebe e organiza os dados enviados da API
    public Alimento(DadosResultadoUSDA dados) {
        this.fdcId = dados.fdcId();
        this.nome = dados.nome();

        this.informacaoNutricional = new InformacaoNutricional(
                dados.informacaoNutricional().getCalorias(),
                dados.informacaoNutricional().getProteinas(),
                dados.informacaoNutricional().getCarboidratos(),
                dados.informacaoNutricional().getGorduras(),
                dados.informacaoNutricional().getSodio(),
                dados.informacaoNutricional().getFibras()
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