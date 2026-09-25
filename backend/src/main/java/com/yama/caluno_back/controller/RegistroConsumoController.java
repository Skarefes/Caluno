package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.dto.DadosDetalhamentoConsumo;
import com.yama.caluno_back.domain.dto.DadosRegistroConsumo;
import com.yama.caluno_back.domain.registro_consumo.RegistroConsumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("consumos")
public class RegistroConsumoController {

    private final RegistroConsumoService registroConsumoService;

    public RegistroConsumoController(RegistroConsumoService registroConsumoService) {
        this.registroConsumoService = registroConsumoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DadosDetalhamentoConsumo>registrarConsumo(@RequestBody DadosRegistroConsumo dadosRegistroConsumo)
    {
        var resultado = registroConsumoService.registrarConsumo(dadosRegistroConsumo);
        return ResponseEntity.created(URI.create("/consumos/" + resultado.id())).body(resultado);
    }

    @GetMapping("/listar-tudo")
    public ResponseEntity<List<DadosDetalhamentoConsumo>> listarTudo(){
        var resultado = registroConsumoService.listarConsumoGeral();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoConsumo>buscarConsumoId(@PathVariable Long id){
        return ResponseEntity.ok().body(registroConsumoService.buscarConsumoId(id));
    }

    //Como é um endpoint que n devolve nada, eu dexei o Response void
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagarConsumoId(@PathVariable Long id){
        registroConsumoService.removerConsumo(id);
        return ResponseEntity.noContent().build();
    }
}
