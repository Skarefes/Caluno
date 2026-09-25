package com.yama.caluno_back.domain.registro_consumo;

import com.yama.caluno_back.domain.InformacaoNutricional;
import com.yama.caluno_back.domain.alimento.Alimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoConsumo;
import com.yama.caluno_back.domain.dto.DadosRegistroConsumo;
import com.yama.caluno_back.domain.repository.AlimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistroConsumoService {

    private final AlimentoRepository alimentoRepository;
    private final RegistroConsumoRepository registroConsumoRepository;

    public RegistroConsumoService(AlimentoRepository alimentoRepository, RegistroConsumoRepository registroConsumoRepository) {
        this.alimentoRepository = alimentoRepository;
        this.registroConsumoRepository = registroConsumoRepository;
    }


    //Função que calcula o consumo de um alimento listado, atravez dos gramas, usando regra de 3, e uma função que multiplique o valor
    public InformacaoNutricional calcularConsumo(Alimento alimento, Double pesoGrama){
        InformacaoNutricional base = alimento.getInformacaoNutricional();
        Double fator = pesoGrama / 100;
        return new InformacaoNutricional(
                multiplicador(base.getCalorias(), fator),
                multiplicador(base.getProteinas(), fator),
                multiplicador(base.getCarboidratos(), fator),
                multiplicador(base.getGorduras(), fator),
                multiplicador(base.getSodio(),fator),
                multiplicador(base.getFibras(), fator)
        );
    }

    //Função que multiplica o valor, para saber a quantidade, e podemos deixar alguns valroes null, ja que nem todos os alimentos contem todas as informações pedidas
    private Double multiplicador(Double valor, double fator){
        return valor!=null ? valor*fator : null;
    }

    //função que vai calcular por unidade
    public InformacaoNutricional calcularConsumoPorUNidade(Alimento alimento, Double quantidadeUiidade){
        return null;
    }

    //Metodo que vai registrar o consumo e salvar no repo, junto com as quantidades e o alimento desejado
    public DadosDetalhamentoConsumo registrarConsumo(DadosRegistroConsumo dadosRegistroConsumo){
        Alimento alimento = alimentoRepository.findById(dadosRegistroConsumo.alimentoId())
                .orElseThrow(() -> new EntityNotFoundException("Alimento não encontrado: " + dadosRegistroConsumo.alimentoId()));

        var registro = new RegistroConsumo(
                alimento,
                dadosRegistroConsumo.quantidade(),
                dadosRegistroConsumo.unidadeTipo(),
                dadosRegistroConsumo.pesoGrama(),
                LocalDateTime.now()
        );

        InformacaoNutricional calculado = calcularConsumo(alimento, dadosRegistroConsumo.pesoGrama());
        registroConsumoRepository.save(registro);

        return new DadosDetalhamentoConsumo(registro, calculado);
    }

    //metodo que vai listar todos os dados registrados com os calculos
    public List<DadosDetalhamentoConsumo>listarConsumoGeral(){
        var registros = registroConsumoRepository.findAll().stream()
                .map(registro -> {InformacaoNutricional calculado = calcularConsumo(registro.getAlimento(), registro.getPesoGrama()); return new DadosDetalhamentoConsumo(registro, calculado);}).collect(Collectors.toList());
        return registros;
    }

    //metodo que vai buscar um alimento por Id, e mostrar o calculo
    public DadosDetalhamentoConsumo buscarConsumoId(Long id){
        var registro = registroConsumoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Registro não encontrado"));
        InformacaoNutricional calculado = calcularConsumo(registro.getAlimento(), registro.getPesoGrama());
        return new DadosDetalhamentoConsumo(registro, calculado);
    }

    //metodo que vai deletar um consumo
    public void removerConsumo(Long id){
        registroConsumoRepository.deleteById(id);
    }
}
