package com.yama.caluno_back.domain.registro_consumo;

import com.yama.caluno_back.domain.alimento.Alimento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table (name = "registro_consumo")
public class RegistroConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Alimento alimento;
    private Double quantidade;
    private String unidadeTipo;
    private Double pesoGrama;
    private LocalDateTime data;

    public RegistroConsumo(Alimento alimento, Double quantidade, String unidadeTipo, Double pesoGrama, LocalDateTime data) {
        this.alimento = alimento;
        this.quantidade = quantidade;
        this.unidadeTipo = unidadeTipo;
        this.pesoGrama = pesoGrama; //Alguns locais eu coloquei pesoGrama e outros pesoEmGrama, preciso arrumar.
        this.data = data;

    }
}
