package com.yama.caluno_back.domain.registro_consumo;

import com.yama.caluno_back.domain.nutricao.DadosResumoNutricional;
import com.yama.caluno_back.domain.nutricao.InformacaoNutricional;
import com.yama.caluno_back.domain.alimento.Alimento;
import com.yama.caluno_back.domain.dto.DadosDetalhamentoConsumo;
import com.yama.caluno_back.domain.dto.DadosRegistroConsumo;
import com.yama.caluno_back.domain.porcao.PorcaoAlimento;
import com.yama.caluno_back.domain.alimento.AlimentoRepository;
import com.yama.caluno_back.domain.usuario.Usuario;
import com.yama.caluno_back.domain.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RegistroConsumoService {

    // Porções que a USDA cadastra mas que NÃO representam uma unidade contável
    // (RACC = porção de referência regulatória, undetermined = sem medida definida)
    private static final Set<String> NAO_UNIDADE = Set.of("racc", "undetermined");

    private final AlimentoRepository alimentoRepository;
    private final RegistroConsumoRepository registroConsumoRepository;
    private final UsuarioRepository usuarioRepository;

    public RegistroConsumoService(AlimentoRepository alimentoRepository, RegistroConsumoRepository registroConsumoRepository, UsuarioRepository usuarioRepository) {
        this.alimentoRepository = alimentoRepository;
        this.registroConsumoRepository = registroConsumoRepository;
        this.usuarioRepository = usuarioRepository;
    }


    //Função que calcula o consumo de um alimento listado, atravez dos gramas, usando regra de 3, e uma função que multiplique o valor
    public InformacaoNutricional calcularConsumo(Alimento alimento, Double pesoGrama) {
        //Coloquei um pequeno if, para caso o usuario coloque um valor null, valor negativo e bug o sistema
        if (alimento == null || alimento.getInformacaoNutricional() == null){
            throw new IllegalArgumentException("O almiento não possui informações nutricionais");
        }
        if (pesoGrama == null || pesoGrama <= 0){
            throw new IllegalArgumentException("O peso deve ser maior que zero");
        }

        InformacaoNutricional base = alimento.getInformacaoNutricional();
        double fator = pesoGrama / 100;
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

    //função que vai calcular a grama
    public Double converterParaGrama(Double quantidade, PorcaoAlimento porcao){

        if (quantidade == null || quantidade <= 0){
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        if (porcao == null
                || porcao.getPesoGrama() == null
                || porcao.getPesoGrama() <= 0
                || porcao.getQuantidade() == null
                || porcao.getQuantidade() <= 0){
            throw new IllegalArgumentException("Porcao  não contem peso ou quantidade corretas");
        }

        // A USDA diz, quantidade unidade pesam pesoGrama gramas
        //Dividindo da o peso de uma unidade
        double gramasPorUnidade = porcao.getPesoGrama() / porcao.getQuantidade();

        return quantidade * gramasPorUnidade;
    }

    //Procura a porção que será usada
    private PorcaoAlimento escolherPorcao(Alimento alimento, String tipoInformado){
        boolean generico = tipoInformado.equalsIgnoreCase("unidade");
        return alimento.getPorcoes().stream()
                // descarta a pocoes sem peso e quantidade validos
                .filter(p -> p.getPesoGrama() != null && p.getPesoGrama() > 0
                        && p.getQuantidade() != null && p.getQuantidade() > 0)
                //unidade aceita qualquer porcao contavel ignorando o RACC e o que eu nao escolhi como unidade
                //qualquer outro texto precisa bater com o nome da porcao
                .filter(p -> generico ? p.getUnidadeTipo() != null
                        && !NAO_UNIDADE.contains(p.getUnidadeTipo().toLowerCase()): tipoInformado.equalsIgnoreCase(p.getUnidadeTipo()))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Nenhuma porção em unidade encontrada para este alimento: " + tipoInformado));
    }

    //Metodo que vai registrar o consumo e salvar no repo, junto com as quantidades e o alimento desejado
    @Transactional
    public DadosDetalhamentoConsumo registrarConsumo(DadosRegistroConsumo dadosRegistroConsumo, String nomeUsuario) {

        Alimento alimento = alimentoRepository.findById(dadosRegistroConsumo.alimentoId())
                .orElseThrow(() -> new EntityNotFoundException("Alimento não encontrado: " + dadosRegistroConsumo.alimentoId()));
        Usuario usuario = usuarioRepository.findByNome(nomeUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado:" +  nomeUsuario));

        Double pesoGrama;
        String unidadeTipo;

        //Sem unidade ou "g" a quantidade ja estará em grama para fascilitar
        if (dadosRegistroConsumo.unidadeTipo() == null || dadosRegistroConsumo.unidadeTipo().equalsIgnoreCase("g")){
            // Caso usuario informou direto em gramas
            pesoGrama = dadosRegistroConsumo.quantidade();
            unidadeTipo = "g";
        } else {
            //vai ter uma unidade generica para o usuario entender, ou o nome da porsao USDA ( entrando como egg, Banana)
            PorcaoAlimento porcaoAlimento = escolherPorcao(alimento, dadosRegistroConsumo.unidadeTipo());
            pesoGrama = converterParaGrama(dadosRegistroConsumo.quantidade(), porcaoAlimento);
            unidadeTipo = dadosRegistroConsumo.unidadeTipo().equalsIgnoreCase("unidade")? "unidade": porcaoAlimento.getUnidadeTipo();

        }
        //Caso ele não envie o grama, ele joga na regra de 3
        InformacaoNutricional calculado = calcularConsumo(alimento, pesoGrama);

        var registro = new RegistroConsumo(
                alimento,
                dadosRegistroConsumo.quantidade(),
                unidadeTipo,
                pesoGrama,
                calculado, //Aqui aonde entraria os nutrientes
                LocalDateTime.now(),
                usuario
        );
        registroConsumoRepository.save(registro);

        return new DadosDetalhamentoConsumo(registro, calculado);
    }

    //metodo que vai listar todos os dados registrados com os calculos do usuario
    @Transactional(readOnly = true)
    public List<DadosDetalhamentoConsumo>listarConsumoGeral(String nomeUsuario) {
        //Como criei um tratador de errros, nao preciso por um try catch aqui
        return registroConsumoRepository.findByUsuarioNome(nomeUsuario).stream()
                .map(registro -> new DadosDetalhamentoConsumo(
                        registro,
                        registro.getInformacaoNutricional() != null ? registro.getInformacaoNutricional()
                                : calcularConsumo(registro.getAlimento(),
                                registro.getPesoGrama()
                        )
                ))
                .collect(Collectors.toList());
    }

    //metodo que vai buscar um alimento por Id, e mostrar o calculo do usuario
    @Transactional(readOnly = true)
    public DadosDetalhamentoConsumo buscarConsumoId(Long id, String nomeUsuario) throws IllegalAccessException {
        var registro = registroConsumoRepository.findByIdAndUsuarioNome(id, nomeUsuario).orElseThrow(() -> new EntityNotFoundException("Registro não encontrado"));
        //InformacaoNutricional calculado = calcularConsumo(registro.getAlimento(), registro.getPesoGrama());
        //return new DadosDetalhamentoConsumo(registro, calculado);
        InformacaoNutricional calculado =
                registro.getInformacaoNutricional() != null
                        ? registro.getInformacaoNutricional()
                        : calcularConsumo(
                        registro.getAlimento(),
                        registro.getPesoGrama()
                );

        return new DadosDetalhamentoConsumo(registro, calculado);
        //Troquei os dados por usarem os registros novos salvos com os valroes com nutrientes

    }

    //metodo que vai deletar um consumo do usuario
    public void removerConsumo(Long id, String nomeUsuario) throws IllegalAccessException {
        var registro = registroConsumoRepository.findByIdAndUsuarioNome(id , nomeUsuario)
                        .orElseThrow(() -> new EntityNotFoundException("Registro não encontrado"));

        registroConsumoRepository.delete(registro);
    }

    @Transactional(readOnly = true)
    public DadosResumoNutricional resumoDoDia(String nomeUsuario, LocalDate dia) throws IllegalAccessException {
        //Pega o dia informado des do inicio do dia
        LocalDateTime inicio = dia.atStartOfDay();
        //Pega o dia seguinte ao informado e define o horario para 00:00:00, garantindo que o dia escolhido nao ultrapasse o outro dia
        LocalDateTime fim = dia.plusDays(1).atStartOfDay();

        //Busco  a lista de registros filtrando por usuario, inicio e fim do dia
        List<RegistroConsumo> registros = registroConsumoRepository.findByUsuarioNomeAndDataGreaterThanEqualAndDataLessThan(nomeUsuario,inicio,fim);

        //Zero as variaveis de nutrientes
        double calorias = 0;
        double proteinas = 0;
        double carboidratos = 0;
        double gorduras = 0;
        double sodio = 0;
        double fibras = 0;

        //Tenta ler os registos ja registrados e soma os valores de cada nutriente do dia
        for (RegistroConsumo registro : registros) {
            InformacaoNutricional info = registro.getInformacaoNutricional();

            //Ajuda na compatibildiadde com registros que não tinham registros salvos
            if (info == null) {
                info = calcularConsumo(registro.getAlimento(), registro.getPesoGrama());

            }

            calorias += valorOuZero(info.getCalorias());
            proteinas += valorOuZero(info.getProteinas());
            carboidratos += valorOuZero(info.getCarboidratos());
            gorduras += valorOuZero(info.getGorduras());
            sodio += valorOuZero(info.getSodio());
            fibras += valorOuZero(info.getFibras());
        }

        return new DadosResumoNutricional(
                dia, calorias, proteinas,  carboidratos, gorduras, sodio, fibras
        );
    }
    //Metodo auxiliar que converte um valor null para 0, permitindo somas
    private double valorOuZero(Double valor){
        return valor != null ? valor : 0.0;
    }
}
