package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.service.AlimentoServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/alimento")
public class AlimentoController {
    private final AlimentoServices alimentoServices;


    public AlimentoController(AlimentoServices alimentoServices) {
        this.alimentoServices = alimentoServices;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DadosDetalhamentoAlimento> registrando(@RequestBody @Valid DadosCadastroAlimento dadosCadastro){
        var criandoDados = alimentoServices.salvarAlimentos(dadosCadastro);
        return ResponseEntity.status(HttpStatus.CREATED).body(criandoDados);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoAlimento> obterAlimentoPorId(@PathVariable Long id){
        var alimento = alimentoServices.buscarAlimentosPorId(id);
        return ResponseEntity.ok(alimento);
    }
}
