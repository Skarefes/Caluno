package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.domain.alimento.AlimentoServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/alimentos")
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

    @GetMapping("/buscar")
    public ResponseEntity<List<InformacaoNutricional>> buscarAlimentoAPI(@RequestParam String nome){
        return ResponseEntity.ok(alimentoServices.buscarAlimentoUSDA(nome));
    }
}
