package com.yama.caluno_back.domain.porcao;

import com.yama.caluno_back.domain.alimento.Alimento;
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
@Table(name = "porcao_alimento")
public class PorcaoAlimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alimento_id", nullable = false)
    private Alimento alimento;
    private Double quantidade;
    private Double pesoGrama;
    private String unidadeTipo;
    private String descricao;

}
