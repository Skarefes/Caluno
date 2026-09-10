package com.yama.caluno_back.service;

import com.yama.caluno_back.domain.alimento.Alimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.domain.repository.AlimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AlimentoServices {
    private final AlimentoRepository alimentoRepository;

    public AlimentoServices(AlimentoRepository alimentoRepository) {
        this.alimentoRepository = alimentoRepository;
    }

    //Metodo que salva um alimento no sistema
    @Transactional
    public DadosDetalhamentoAlimento salvarAlimentos(DadosCadastroAlimento dadosAlimentos){
        var alimento = new Alimento(dadosAlimentos);
        alimentoRepository.save(alimento);
        return new DadosDetalhamentoAlimento(alimento);
    }

    //Metodo que busca um alimento por Id
    public DadosDetalhamentoAlimento buscarAlimentosPorId(Long idAlimento){
        var alimento = alimentoRepository.findById(idAlimento)
                .orElseThrow(() -> new EntityNotFoundException("Alimento não encontrado: " + idAlimento));
        return new DadosDetalhamentoAlimento(alimento);
    }

    public void obterInformacoes(){

    }

    public DadosDetalhamentoAlimento atualizarAlimentos(DadosAtualizacaoAlimento atualizando){

    }
    public void removerAlimentos(){

    }
}
