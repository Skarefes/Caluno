package com.yama.caluno_back.infra.api;

import com.yama.caluno_back.domain.InformacaoNutricional;
import  org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class USDAService {
    //Numeração da localização das informações desejadas pelo numero de identifação dos nutrientes
    //Existem numeros registrados diferentes de fibras no banco da USDA
    private static final List<String> CALORIAS = List.of("208", "957","958");
    private static final List<String> PROTEINA = List.of("203");
    private static final List<String> GORDURA = List.of("204");
    private static final List<String> CARBOIDRATO = List.of("205");
    private static final List<String> SODIO = List.of("307");
    //Existem numeros registrados diferentes de fibras no banco da USDA
    private static final List<String> FIBRA = List.of("291","293");

    //variaveis que terão as variaveis de ambiente do properties
    private final String apiUrl;
    private final String apiKey;
    //Ferramenta que faz a requisição HTTP
    private final RestClient restClient;

    //Criando a variavel da url da api no construtor
    public USDAService(
            @Value("${usda.api.url}") String apiUrl,
            @Value("${usda.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    //Metodo que vai buscar o nome que quisermos no parametro alimentar, e ele vai buscar, seguindo a chave da API, nome, e os parametros de URL pedidos
    public List<InformacaoNutricional> buscarPorNome(String nome){
        DadosBuscaUSDA resposta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/foods/search")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", nome)
                        .queryParam("pageSize", 10)
                        //A foundation, é uma parte para ele encontrar itens sem ter marcas
                        .queryParam("dataType", "Foundation")
                        .build()).retrieve().body(DadosBuscaUSDA.class);
        return resposta.foods().stream().map(this::extrairResumo).toList();
    }

    //Metodo que faz o filtro dentro da lista de nutrientes de um determinado alimento, e procura o alimento com a base do numero que vier primeiro
    private Double buscarValor(DadosAlimentoUSDA food, List<String> candidatos) {
        for (String numero : candidatos) {
            //Optional vai mostrar se encontrou um valor ou não
            Optional<Double> valor = food.foodNutrients().stream()
                    .filter(n -> numero.equals(n.nutrientNumber()))
                    .map(DadosNutrienteUSDA::value)
                    .findFirst();
            if (valor.isPresent()) { //isPresent vai ver se tem algum valor
                return valor.get();
            }
        }
        return null;
    }

    //metodo que cria a request, dispara a chamada e converte em JSON do Record
    private InformacaoNutricional extrairResumo(DadosAlimentoUSDA food) {
        return new InformacaoNutricional(
                buscarValor(food, CALORIAS),
                buscarValor(food, PROTEINA),
                buscarValor(food, CARBOIDRATO),
                buscarValor(food, GORDURA),
                buscarValor(food, SODIO),
                buscarValor(food, FIBRA)
        );
    }

    //Metodo que faz o filtro detro da lista de nutrientes de um determinado alimento
//    private Double buscarValor(DadosAlimentoUSDA food, List<String> nutrientNumber){
//        return food.foodNutrients().stream()
//                .filter(n -> nutrientNumber.equals(n.nutrientNumber()))
//                .map(DadosNutrienteUSDA::value)
//                .findFirst().orElse(null);
//    }


}
