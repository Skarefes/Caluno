package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.alimento.Alimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.domain.alimento.AlimentoServices;
import com.yama.caluno_back.infra.api.USDAService;
import com.yama.caluno_back.infra.api.dto.DadosResultadoUSDA;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/alimentos")
public class AlimentoController {
    private final AlimentoServices alimentoServices;
    private final USDAService usdaService;


    public AlimentoController(AlimentoServices alimentoServices, USDAService usdaService) {
        this.alimentoServices = alimentoServices;
        this.usdaService = usdaService;
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

    //Pode buscar na API o nome do alimento, porem ele ainda precisa ser pesquisado em ingles
    @GetMapping("/buscar")
    public ResponseEntity<List<DadosResultadoUSDA>> buscarAlimentoAPI(@RequestParam String nome){
        return ResponseEntity.ok(alimentoServices.buscarAlimentoUSDA(nome));
    }

    //Salvo qual alimento quero pelo FDCID, ele salva e quando salva manda qual alimento no corpo do JSON para analisar
    //Usa-se os DadosDetalhamentoAlimento para que a entidadeAlimento no seja diretament exposta
    @PostMapping("/salvar")
    public ResponseEntity<DadosDetalhamentoAlimento> salvarAlimentoUSDA(@RequestBody DadosResultadoUSDA dados){
        Alimento alimento = alimentoServices.salvarAlimentoFDCID(dados);
        return ResponseEntity.created(URI.create("/alimentos/" + alimento.getId())).body(new DadosDetalhamentoAlimento(alimento));
    }

    @GetMapping("/usda/{fdcId}")
    public DadosResultadoUSDA buscarAlimentoPorFdcId(@PathVariable Long fdcId){
        return usdaService.buscarDetalhesPorId(fdcId);
    }

}
