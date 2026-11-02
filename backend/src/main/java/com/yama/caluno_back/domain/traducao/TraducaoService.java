package com.yama.caluno_back.domain.traducao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TraducaoService {
    private final TraducaoRepository traducaoRepository;
    private final RestClient restClient = RestClient.create("https://api.mymemory.translated.net"); //Para conseguir usar a API de traduçao
    private final String email; // Usando um email para que eles liberem 50 mil caracteres diario

    public TraducaoService(TraducaoRepository traducaoRepository,
                           @Value("${traducao.email}") String email) {
        this.traducaoRepository = traducaoRepository;
        this.email = email;
    }

    //Metodo que ajuda a remover acentos e espaços de palavras, facilitando a traducao
    private String normalizarPalavra(String texto){
        return Normalizer.normalize(texto.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}","");
        //NFD separa a letra base do acento por dois caracteres independentes á vira a
        //\p{M} essa marcacao representa caracteres combinados invisiveis
    }

    //FUnção que vai traduzier de Pt-Br para En e vice versa
    public String traduzir(String texto, String de, String para){
        String chave = normalizarPalavra(texto);
        return traducaoRepository.findByOrigemAndIdiomaOrigemAndIdiomaDestino(chave, de, para)
                .map(Traducao::getTraducao).orElseGet(() -> {
                    String resultado = chamarApi(texto, de, para);
                    if (resultado == null) {
                        return texto;
                    }

                    //Caso a tradução seja igual ao original, a API nao vai traduzir consumindo menos characteres
                    if (resultado.equalsIgnoreCase(texto.strip())){
                        return texto;
                    }

                    traducaoRepository.inserirSeNaoExistir(chave, de, para, resultado);
                    return resultado;
                });
    }

    //metodo que chama a API para pegar as palavras
    public String chamarApi(String texto, String de, String para){
        try{
            var resposta = restClient.get().uri(u -> u.path("/get")
                    .queryParam("q", "{q}")
                    .queryParam("langpair", "{par}")
                    .queryParam("de", "{email}")
                            .build(Map.of("q", texto,
                                    "par", de + "|" + para, "email", email))) //Mapear as variaveis ajudou o endereço ser mais preciso
                    .retrieve().body(RespostaMyMemory.class);
            if (resposta == null || resposta.responseStatus != 200 || resposta.responseData() == null) {
                return null;
            }

            String traduzido = resposta.responseData().translatedText();
            //Descartei tbm o aviso de cota da API MYMEMORY para acabar nao sendo salvo por engano
            if (traduzido == null || traduzido.isBlank() || traduzido.toUpperCase().contains("MYMEMORY WARNING")) {
                return null;
            }

            //retorna limpo o caracteres HTML e espaços invisiveis  usando o HtmlUtils replace e strip
            return HtmlUtils.htmlUnescape(traduzido).replace('\u00A0', ' ').strip();

        } catch (Exception e) {
            log.warn("Falha ao chamar Mymemory para [{}]", texto, e);
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespostaMyMemory(int responseStatus, ResponseData responseData) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record ResponseData(String translatedText) {}
    }
}
