package com.yama.caluno_back.domain.alimento;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.dto.DadosAtualizacaoAlimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.domain.repository.AlimentoRepository;
import com.yama.caluno_back.infra.api.USDAService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlimentoServices {
    private final AlimentoRepository alimentoRepository;
    private final USDAService usdaService;

    public AlimentoServices(AlimentoRepository alimentoRepository, USDAService usdaService) {
        this.alimentoRepository = alimentoRepository;
        this.usdaService = usdaService;
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

    //Metodo que altera alguma informação do alimento
    @Transactional
    public DadosDetalhamentoAlimento atualizarAlimentos(Long id, DadosAtualizacaoAlimento atualizando){
        //o identificador pega o id no repository
        var alimentoId = alimentoRepository.findById(id).orElseThrow();
        //joga a informação para ser atualizada
        alimentoId.atualizarInformacaoNutricional(atualizando);
        //retorna um novo DTO com os novos dados
        return  new DadosDetalhamentoAlimento(alimentoId);
    }

    //Metodo que remove um alimento
    public void removerAlimentos(Long id){
        alimentoRepository.deleteById(id);
    }

    //Metodo que vai buscar um alimento no banco de dados da API, mais detalhadamente pelo nome do alimento
    public List<InformacaoNutricional> buscarAlimentoUSDA(String nome){
        return usdaService.buscarPorNome(nome);
    }

}
