package com.yama.caluno_back.domain.alimento;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.porcao.DadosPorcaoUSDA;
import com.yama.caluno_back.domain.porcao.PorcaoAlimento;
import com.yama.caluno_back.domain.registro_consumo.RegistroConsumo;
import com.yama.caluno_back.domain.dto.DadosAtualizacaoAlimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.infra.api.dto.DadosResultadoUSDA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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
    //mappedBy = usda o nome do CAMPO na classe PorcaoAlimentos que aponta para a classe Alimento
    @OneToMany(mappedBy = "alimento", cascade = CascadeType.ALL)
    private List<PorcaoAlimento> porcoes = new ArrayList<>();

    //metodo que traz sincronia em relação ao alimento, evita esquecer de preencher o alimento dentro da porcao.
    public void adicionarPorcao(PorcaoAlimento porcao){
        porcoes.add(porcao);
        porcao.setAlimento(this);
    }

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
        if (dados.porcoes() != null) {
            for (DadosPorcaoUSDA p : dados.porcoes()) {
                PorcaoAlimento porcao = new PorcaoAlimento();
                porcao.setQuantidade(p.quantidade());
                porcao.setPesoGrama(p.pesoGrama());
                porcao.setUnidadeTipo(p.unidadeTipo());
                porcao.setDescricao(p.descricao());
                adicionarPorcao(porcao); //Adicionei na lista, ja que o JSON nessa parte porção ficava null
            }
        }
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