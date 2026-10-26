package com.yama.caluno_back.controller;

import com.yama.caluno_back.domain.dto.DadosDetalhamentoConsumo;
import com.yama.caluno_back.domain.dto.DadosRegistroConsumo;
import com.yama.caluno_back.domain.nutricao.DadosResumoNutricional;
import com.yama.caluno_back.domain.registro_consumo.RegistroConsumoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("consumos")
public class RegistroConsumoController {

    private final RegistroConsumoService registroConsumoService;

    public RegistroConsumoController(RegistroConsumoService registroConsumoService) {
        this.registroConsumoService = registroConsumoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DadosDetalhamentoConsumo>registrarConsumo(@RequestBody @Valid DadosRegistroConsumo dadosRegistroConsumo, Authentication authentication) throws IllegalAccessException {
        var resultado = registroConsumoService.registrarConsumo(dadosRegistroConsumo, authentication.getName());
        return ResponseEntity.created(URI.create("/consumos/" + resultado.id())).body(resultado);
    }

    @GetMapping("/listar-tudo")
    public ResponseEntity<List<DadosDetalhamentoConsumo>> listarTudo(Authentication authentication) {
        var resultado = registroConsumoService.listarConsumoGeral(authentication.getName());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoConsumo>buscarConsumoId(@PathVariable Long id, Authentication authentication) throws IllegalAccessException {
        return ResponseEntity.ok().body(registroConsumoService.buscarConsumoId(id, authentication.getName()));
    }

    //Como é um endpoint que n devolve nada, eu dexei o Response void
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagarConsumoId(@PathVariable Long id,  Authentication authentication) throws IllegalAccessException {
        registroConsumoService.removerConsumo(id,  authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo-diario")
    public DadosResumoNutricional resumoDiario(Authentication authentication, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) throws IllegalAccessException {
        LocalDate dia = data != null ? data : LocalDate.now();

        return registroConsumoService.resumoDoDia(authentication.getName(), dia);
    }
}
