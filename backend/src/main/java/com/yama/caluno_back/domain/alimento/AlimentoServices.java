package com.yama.caluno_back.domain.alimento;

import com.yama.caluno_back.domain.dto.DadosAtualizacaoAlimento;
import com.yama.caluno_back.domain.dto.DadosCadastroAlimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoAlimento;
import com.yama.caluno_back.domain.traducao.IdiomaBusca;
import com.yama.caluno_back.domain.traducao.TraducaoService;
import com.yama.caluno_back.infra.api.dto.DadosResultadoUSDA;
import com.yama.caluno_back.infra.api.USDAService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AlimentoServices {
    private final AlimentoRepository alimentoRepository;
    private final USDAService usdaService;
    private final TraducaoService traducaoService;

    //Padronizando ja que é um erro humano escrever errado muitas vezes
    public static final String PT_BR = "pt-BR";
    public static final String EN = "en";

    public AlimentoServices(AlimentoRepository alimentoRepository, USDAService usdaService, TraducaoService traducaoService) {
        this.alimentoRepository = alimentoRepository;
        this.usdaService = usdaService;
        this.traducaoService = traducaoService;
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

    //Busca o alimento com detalhes, porem com a tradução do alimento
    public DadosResultadoUSDA buscarAlimentoDetalheUSDA(Long fdcId){
        DadosResultadoUSDA r = usdaService.buscarDetalhesPorId(fdcId);

        return new DadosResultadoUSDA(r.fdcId(), traducaoService.traduzir(r.nome(), EN, PT_BR),
                r.dataType(), r.informacaoNutricional(), r.porcoes());
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
    public List<DadosResultadoUSDA> buscarAlimentoUSDA(String nome, IdiomaBusca idiomaBusca){
        //Modo ingles: o usuario ja escreveu em ingles, entao nao traduz nada, nem a busca nem os resultados
        if (idiomaBusca == IdiomaBusca.EN) {
            return usdaService.buscarPorNome(nome);
        }

        //Modo portugues: traduzindo/ o controller por padrao começa em PT-BR
        String termoIngles = traducaoService.traduzir(nome, PT_BR, EN);
        log.info("Termo enviado a USDA: [{}]", termoIngles);

        var resultados = usdaService.buscarPorNome(termoIngles);

        if (resultados.isEmpty() && !termoIngles.equalsIgnoreCase(nome)) {
            resultados = usdaService.buscarPorNome(nome);
        }

        return resultados.stream()
                .map(r -> new DadosResultadoUSDA(
                        r.fdcId(), traducaoService.traduzir(r.nome(), EN, PT_BR),
                        r.dataType(), r.informacaoNutricional(), r.porcoes()
                )).toList();
    }

    //Metodo que vai pegar o fdcId escolhido e salvar no banco de dados para o usuario
    //Se ja existe, devolve oque esta salvo, caso não ele cria um novo
    @Transactional
    public Alimento salvarAlimentoFDCID(Long fdcId){
        return alimentoRepository.findByFdcId(fdcId)
                .orElseGet(() -> {
                    DadosResultadoUSDA dados = usdaService.buscarDetalhesPorId(fdcId);
                    Alimento alimento = new Alimento(dados);
                    alimento.setNome(traducaoService.traduzir(dados.nome(), EN, PT_BR));
                    return alimentoRepository.save(alimento);
                });
    }

}
