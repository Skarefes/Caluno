package com.yama.caluno_back.domain.traducao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "traducao", uniqueConstraints = @UniqueConstraint(columnNames = {"origem","idioma_origem","idioma_destino"}))
public class Traducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String origem; // palavra que vai originar
    private String idiomaOrigem; //Aqui é Pt-Br ou EN
    private String idiomaDestino; //Aqui qual ele se tornou
    private String traducao; // ex. Se for apple ou maça

    public Traducao(String origem, String idiomaOrigem, String idiomaDestino, String traducao) {
        this.origem = origem;
        this.idiomaOrigem = idiomaOrigem;
        this.idiomaDestino = idiomaDestino;
        this.traducao = traducao;
    }
}
