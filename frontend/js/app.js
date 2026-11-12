// Valores que o back compara em DadosRegistroConsumo.unidadeTipo
const UNIDADE_GRAMA = "g";
const ML_POR_UNIDADE = { "ml": 1, "l": 1000, "fl oz": 29.5735, "cup": 236.588, "tbsp": 14.7868, "tsp": 4.92892 };
const UNIDADES_VOLUME = new Set(["ml", "l"]);

// Só tradução do que aparece na tela. O valor enviado ao back continua o original em inglês.
// Apareceu uma porção nova em inglês? É só adicionar uma linha aqui.
const TAMANHOS = {
  "extra small": "extra pequeno",
  "small": "pequeno",
  "medium": "médio",
  "large": "grande",
  "extra large": "extra grande",
  "serving": "porcão",
  "strip": "pedaço",
  "piece": "pedaço",
  "slice": "fatia",
  "fillet": "filé",
  "drumstick": "coxa",
  "thigh": "sobrecoxa",
  "wing": "asa",
  "unit": "unidade",
  "l":"L",
  "can or bottle": "lata ou garrafa",
  "bottle": "garrafa",
  "can": "lata"
};

const ESPERA_BUSCA_MS = 800;   // tempo parado até buscar sozinho
const MINIMO_CARACTERES = 3;   // abaixo disso não busca

const $ = (id) => document.getElementById(id);
const formatador = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 });

// Tudo que muda durante o uso fica num só objeto: fácil de achar e de depurar.
const estado = {
  alimentoEscolhido: null, // DadosResultadoUSDA
  resultadosBusca: [],
  registros: [],           // lista de DadosDetalhamentoConsumo
  timerBusca: null,
  controleBusca: null      // permite cancelar a busca anterior
};

/* ---------- Nome automatico ---------- */

const nomeUsuario = sessionStorage.getItem("calunoNome");

const accountLabel = document.getElementById("account-label");
const accountAvatar = document.getElementById("account-avatar");

if (nomeUsuario) {
    accountLabel.textContent = nomeUsuario;
    accountAvatar.textContent = nomeUsuario.charAt(0).toUpperCase();
}


/* ---------- Utilidades ---------- */

function formatar(valor) {
  const n = Number(valor);
  return Number.isFinite(n) ? formatador.format(n) : "—";
}

function mostrarMensagem(texto, ehErro = false, alvo = "global-message") {
  $(alvo).textContent = texto;
  $(alvo).classList.toggle("error", ehErro);
}

function hojeEmTexto() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mes}-${dia}`; // formato do <input type="date"> e do LocalDate
}

function mostrarStatusBusca(html) {
  $("search-state").hidden = false;
  $("search-state").innerHTML = html;
}

// Cada função abaixo = um endpoint do back.
const api = {
  buscarAlimentos: (nome, idioma, signal) =>
    chamarApi(`/alimentos/buscar?nome=${encodeURIComponent(nome)}&idioma=${idioma}`, { signal }),
  detalharUsda:    (fdcId) => chamarApi(`/alimentos/usda/${fdcId}`),
  salvarAlimento:  (fdcId) => chamarApi(`/alimentos/salvar/${fdcId}`, { method: "POST" }),
  registrarConsumo:(corpo) => chamarApi("/consumos/registrar", { method: "POST", body: JSON.stringify(corpo) }),
  listarConsumos:  () => chamarApi("/consumos/listar-tudo"),
  resumoDoDia:     (data) => chamarApi(`/consumos/resumo-diario?data=${data}`),
  apagarConsumo:   (id) => chamarApi(`/consumos/${id}`, { method: "DELETE" })
};

/* ---------- Etapa 1: busca ---------- */

async function pesquisar(texto) {
  if (estado.controleBusca) estado.controleBusca.abort();
  estado.controleBusca = new AbortController();
  $("search-results").replaceChildren();

  if (texto.length < MINIMO_CARACTERES) {
    $("results-count").textContent = "";
    mostrarStatusBusca(`<strong>Continue digitando</strong><span>Informe pelo menos ${MINIMO_CARACTERES} caracteres.</span>`);
    return;
  }

  mostrarStatusBusca('<span class="spinner"></span><span>Pesquisando alimentos...</span>');
  try {
    estado.resultadosBusca = await api.buscarAlimentos(
      texto,
      $("search-language").value,
      estado.controleBusca.signal
    );
    mostrarResultados();
  } catch (erro) {
    if (erro.name === "AbortError") return; // busca antiga cancelada: normal
    mostrarStatusBusca("<strong>Não foi possível pesquisar</strong><span>Verifique se o backend está ativo.</span>");
    mostrarMensagem(erro.message, true);
  }
}

// Ajusta placeholder e aviso conforme o idioma escolhido
function atualizarIdiomaBusca() {
  const emIngles = $("search-language").value === "EN";
  $("food-search").placeholder = emIngles
    ? "Type the food name in English (e.g. rice)"
    : "Ex.: arroz, banana, frango...";
  $("search-hint").hidden = emIngles; // o aviso só faz sentido no modo português
}

function mostrarResultados() {
  const lista = $("search-results");
  lista.replaceChildren();
  $("results-count").textContent = `${estado.resultadosBusca.length} encontrado(s)`;

  if (estado.resultadosBusca.length === 0) {
    mostrarStatusBusca("<strong>Nenhum alimento encontrado</strong><span>Tente outro termo.</span>");
    return;
  }
  $("search-state").hidden = true;

  estado.resultadosBusca.forEach((alimento) => {
    const botao = document.createElement("button");
    botao.type = "button";
    botao.className = "food-item";

    const nome = document.createElement("span");
    nome.className = "food-item-name";
    nome.textContent = alimento.nome;              // campo do DadosResultadoUSDA
    const codigo = document.createElement("span");
    codigo.className = "food-item-code";
    codigo.textContent = `FDC ID: ${alimento.fdcId}`;
    const info = document.createElement("span");
    info.append(nome, codigo);

    const kcal = document.createElement("span");
    kcal.className = "food-item-kcal";
    const calorias = alimento.informacaoNutricional?.calorias;
    kcal.textContent = calorias ? `${formatar(calorias)} kcal / 100 g` : "Ver detalhes";

    botao.append(info, kcal);
    botao.addEventListener("click", () => escolherAlimento(alimento));
    lista.append(botao);
  });
}

/* ---------- Etapa 2: porção ---------- */

// Quando troca a unidade, a quantidade antiga deixa de fazer sentido
// (200 g é normal, mas 200 bananas não). Volta para um valor padrão.

function trocarUnidade() {
  const unidade = $("portion-unit").value;
  $("portion-quantity").value = (unidade === UNIDADE_GRAMA || unidade === "ml") ? "100" : "1";
  atualizarPreviaPorcao(); // recalcula as calorias ou ml com a nova quantidade
}

// 'medium (7" to 7-7/8" long)' -> 'médio'. Se não estiver no mapa, mostra o texto original.
function nomeDaPorcao(descricao) {
  const nome = descricao.split("(")[0].trim().toLowerCase();
  return TAMANHOS[nome] ?? descricao;
}

// 'medium (7" ...)' -> 'medium'
function nomeBase(descricao) {
  return descricao.split("(")[0].trim().toLowerCase();
}

// g/ml calculado a partir de uma porção em volume da USDA, ou null se não houver
function densidadeDoAlimento() {
  for (const p of estado.alimentoEscolhido?.porcoes || []) {
    if (!(p.pesoGrama > 0 && p.quantidade > 0)) continue;
    const mlPorUnidade = ML_POR_UNIDADE[nomeBase(p.descricao)];
    if (mlPorUnidade) return p.pesoGrama / (p.quantidade * mlPorUnidade);
  }
  return null;
}

// acha a porção do alimento escolhido pela descrição (é o valor que o select guarda)
function acharPorcao(descricao) {
  return (estado.alimentoEscolhido?.porcoes || []).find((p) => p.descricao === descricao) || null;
}

// converte o que o usuário digitou para gramas, seja "g" ou uma porção
function gramasDaPorcao() {
  const quantidade = Number($("portion-quantity").value);
  const escolha = $("portion-unit").value;

  if (escolha === UNIDADE_GRAMA) return quantidade;

  if (UNIDADES_VOLUME.has(escolha)){
    const densidade = densidadeDoAlimento();
    return densidade?quantidade * ML_POR_UNIDADE[escolha] * densidade : 0;
  }

  const porcao = acharPorcao(escolha);
  // gramas de UMA unidade = pesoGrama / quantidade (a mesma conta do back)
  return porcao ? quantidade * (porcao.pesoGrama / porcao.quantidade) : 0;
}

// monta o select: gramas sempre + uma opção por porção que veio da USDA
function atualizarOpcoesDeUnidade() {
  const seletor = $("portion-unit");
  seletor.replaceChildren(new Option("gramas (g)", UNIDADE_GRAMA));

  // ml e L só aparecem se a USDA deu uma medida de volume para converter
  if (densidadeDoAlimento()) {
    seletor.add(new Option("mililitros (ml)", "ml"));
    seletor.add(new Option("litros (L)", "l"));
  }

  (estado.alimentoEscolhido?.porcoes || []).forEach((p) => {
    if (!(p.pesoGrama > 0 && p.quantidade > 0)) return;
    // fl oz, cup etc. servem só para calcular a densidade, não viram opção
    if (ML_POR_UNIDADE[nomeBase(p.descricao)]) return;
    const gramas = Math.round(p.pesoGrama / p.quantidade);
    seletor.add(new Option(`${nomeDaPorcao(p.descricao)} (~${gramas} g)`, p.descricao));
  });
}

async function escolherAlimento(alimento) {
  estado.alimentoEscolhido = alimento;
  // NOVO: volta o seletor para gramas a cada alimento novo
  $("portion-unit").replaceChildren(new Option("gramas (g)", UNIDADE_GRAMA));
  $("no-selection").hidden = true;
  $("selected-food").hidden = false;
  $("selected-name").textContent = alimento.nome;
  $("selected-description").textContent = `Código USDA: ${alimento.fdcId}`;
  atualizarOpcoesDeUnidade(); // NOVO: a busca já pode ter trazido porções
  atualizarPreviaPorcao();
  mostrarMensagem("", false, "portion-message");

  try {
    // Busca os detalhes completos (inclui porções) e mescla com o que já temos.
    const detalhe = await api.detalharUsda(alimento.fdcId);
    estado.alimentoEscolhido = { ...alimento, ...detalhe };
    atualizarOpcoesDeUnidade(); // NOVO
    atualizarPreviaPorcao();
  } catch (_) {
    mostrarMensagem("Não consegui carregar os detalhes completos.", true, "portion-message");
  }
}

function atualizarPreviaPorcao() {
  if (!estado.alimentoEscolhido) return;
  const gramas = gramasDaPorcao(); // ALTERADO: antes era Number($("portion-quantity").value)
  const fator = gramas > 0 ? gramas / 100 : 0; // valores da USDA são por 100 g
  const n = estado.alimentoEscolhido.informacaoNutricional || {};

  $("portion-calories").innerHTML = `${formatar((n.calorias || 0) * fator)} <small>kcal</small>`;
  $("portion-protein").textContent = `Proteínas: ${formatar((n.proteinas || 0) * fator)} g`;
  $("portion-carbs").textContent = `Carboidratos: ${formatar((n.carboidratos || 0) * fator)} g`;
  $("portion-fat").textContent = `Gorduras: ${formatar((n.gorduras || 0) * fator)} g`;
}

function limparSelecao() {
  estado.alimentoEscolhido = null;
  $("selected-food").hidden = true;
  $("no-selection").hidden = false;
  $("portion-quantity").value = "100";
  $("portion-unit").replaceChildren(new Option("gramas (g)", UNIDADE_GRAMA)); // NOVO
}

async function adicionarAoDiario(evento) {
  evento.preventDefault(); // impede o formulário de recarregar a página
  const alimento = estado.alimentoEscolhido;
  const quantidade = Number($("portion-quantity").value);
  if (!alimento || !(quantidade > 0)) {
    mostrarMensagem("Escolha um alimento e informe uma quantidade válida.", true, "portion-message");
    return;
  }

  const botao = $("add-food-button");
  botao.disabled = true;
  try {
    // 1) salva o alimento no seu banco (devolve DadosDetalhamentoAlimento com o id LOCAL)
    const salvo = await api.salvarAlimento(alimento.fdcId);
    // 2) registra o consumo usando esse id local, não o fdcId
    // ALTERADO: manda a unidade escolhida ("g" ou "unidade") em vez da constante fixa
    await api.registrarConsumo({
      alimentoId: salvo.id,
      quantidade,
      unidadeTipo: $("portion-unit").value
    });

    mostrarMensagem("Alimento adicionado ao seu diário!", false, "portion-message");
    limparSelecao();
    $("food-search").value = "";
    $("clear-search").hidden = true;
    $("search-results").replaceChildren();
    mostrarStatusBusca("<strong>Alimento registrado</strong><span>Você pode pesquisar outro.</span>");
    await carregarDiario();
  } catch (erro) {
    mostrarMensagem(`Não foi possível registrar: ${erro.message}`, true, "portion-message");
  } finally {
    botao.disabled = false;
  }
}

/* ---------- Diário ---------- */

async function carregarDiario() {
  $("diary-loading").hidden = false;
  $("diary-error").hidden = true;
  try {
    estado.registros = await api.listarConsumos();
    mostrarDiario();
    await carregarResumoDoDia();
  } catch (erro) {
    $("diary-loading").hidden = true;
    $("diary-error").hidden = false;
    mostrarMensagem(`Erro ao carregar o diário: ${erro.message}`, true);
  }
}

//metodo que atualiza os nutrientes sem transformara valroes nulos em 0
function atualizarNutriente(id, valor){
  $(id).textContent = valor == null ? "—" : formatar(valor);
}

// Totais do dia calculados pelo BACK (DadosResumoNutricional)
async function carregarResumoDoDia() {
  try {
        const resumo = await api.resumoDoDia($("diary-date").value);

        // Atualiza o resumo superior que já existia
        $("daily-calories").textContent = formatar(resumo.calorias);

        $("diary-total-calories").innerHTML =
            `${formatar(resumo.calorias)} <small>kcal</small>`;

        // Atualiza os seis cartões do painel expansível
        atualizarNutriente("nutrition-calories", resumo.calorias);
        atualizarNutriente("nutrition-carbs", resumo.carboidratos);
        atualizarNutriente("nutrition-protein", resumo.proteinas);
        atualizarNutriente("nutrition-fat", resumo.gorduras);
        atualizarNutriente("nutrition-fiber", resumo.fibras);
        atualizarNutriente("nutrition-sodium", resumo.sodio);

    } catch (erro) {
        console.error("Erro ao carregar resumo nutricional:", erro);
    }
}

function mostrarDiario() {
  $("diary-loading").hidden = true;
  const dia = $("diary-date").value;
  // "data" vem como "2026-11-02T14:30:00": os 10 primeiros caracteres são a data
  const doDia = estado.registros.filter((r) => String(r.data).slice(0, 10) === dia);

  $("food-count").textContent = doDia.length;
  $("diary-empty").hidden = doDia.length > 0;
  $("summary-date").textContent = `Resumo de ${dia.split("-").reverse().join("/")}`;
  $("diary-list").replaceChildren();

  doDia.forEach((registro) => $("diary-list").append(criarLinhaDiario(registro)));
}

// Monta a linha de nutrientes; pula os que vieram null (nem todo alimento tem todos)
function criarNutrientes(info) {
  const itens = [
    ["Prot", info?.proteinas, "g"],
    ["Carb", info?.carboidratos, "g"],
    ["Gord", info?.gorduras, "g"],
    ["Fibras", info?.fibras, "g"],
    ["Sódio", info?.sodio, "mg"]
  ];
  const caixa = document.createElement("div");
  caixa.className = "diary-item-nutrients";

  itens.forEach(([rotulo, valor, unidade]) => {
    if (valor == null) return;
    const chip = document.createElement("span");
    chip.textContent = `${rotulo} ${formatar(valor)} ${unidade}`;
    caixa.append(chip);
  });
  return caixa;
}

function criarLinhaDiario(registro) {
  const linha = document.createElement("article");
  linha.className = "diary-item";

  const icone = document.createElement("span");
  icone.className = "diary-item-icon";
  icone.textContent = "◒";

  const info = document.createElement("div");
  info.className = "diary-item-info";
  const nome = document.createElement("strong");
  nome.textContent = registro.nomeAlimento;
  const detalhe = document.createElement("span");
  detalhe.textContent = `${formatar(registro.quantidade)} ${nomeDaPorcao(registro.unidadeTipo)}`;
  info.append(nome, detalhe, criarNutrientes(registro.informacao));

  const kcal = document.createElement("strong");
  kcal.className = "diary-item-kcal";
  kcal.textContent = `${formatar(registro.informacao?.calorias)} kcal`;

  const apagar = document.createElement("button");
  apagar.className = "diary-item-delete";
  apagar.type = "button";
  apagar.textContent = "×";
  apagar.setAttribute("aria-label", `Excluir ${registro.nomeAlimento}`);
  apagar.addEventListener("click", () => apagarRegistro(registro.id, apagar));

  linha.append(icone, info, kcal, apagar);
  return linha;
}

async function apagarRegistro(id, botao) {
  if (!confirm("Deseja excluir este alimento do seu diário?")) return;
  botao.disabled = true;
  try {
    await api.apagarConsumo(id);
    mostrarMensagem("Registro removido do diário.");
    await carregarDiario();
  } catch (erro) {
    mostrarMensagem(`Não foi possível excluir: ${erro.message}`, true);
    botao.disabled = false;
  }
}

/* ---------- Eventos e inicialização ------- */

function ligarEventos() {
  ["food-search","clear-search","search-language","switch-to-english",
 "portion-quantity","portion-unit","portion-form","change-food",
 "retry-diary","diary-date"].forEach((id) => {
  if (!$(id)) console.error(`Falta no HTML o id: ${id}`);
  });
  $("food-search").addEventListener("input", () => {
    const texto = $("food-search").value.trim();
    $("clear-search").hidden = texto === "";
    clearTimeout(estado.timerBusca); // debounce: espera parar de digitar
    estado.timerBusca = setTimeout(() => pesquisar(texto), ESPERA_BUSCA_MS);
  });

  // Enter busca na hora, sem esperar o tempo
  $("food-search").addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") {
      clearTimeout(estado.timerBusca); // cancela a busca agendada para não buscar 2x
      pesquisar($("food-search").value.trim());
    }
  });

  $("clear-search").addEventListener("click", () => {
    clearTimeout(estado.timerBusca);
    $("food-search").value = "";
    $("clear-search").hidden = true;
    pesquisar("");
  });

  $("search-language").addEventListener("change", () => {
    atualizarIdiomaBusca();
    const texto = $("food-search").value.trim();
    if (texto) pesquisar(texto);
  });

  $("switch-to-english").addEventListener("click", () => {
    clearTimeout(estado.timerBusca);
    $("search-language").value = "EN";
    atualizarIdiomaBusca();
    $("food-search").value = "";   // o termo em português não serve em inglês
    $("clear-search").hidden = true;
    pesquisar("");                 // limpa os resultados antigos
    $("food-search").focus();
  });

  $("portion-quantity").addEventListener("input", atualizarPreviaPorcao);
  $("portion-unit").addEventListener("change", trocarUnidade); // NOVO
  $("portion-form").addEventListener("submit", adicionarAoDiario);
  $("change-food").addEventListener("click", () => $("food-search").focus());
  $("retry-diary").addEventListener("click", carregarDiario);

  
  // Abre e recolhe o resumo nutricional
  $("nutrition-toggle").addEventListener("click", () => {
      const botao = $("nutrition-toggle");
      const painel = $("nutrition-panel");

      const aberto = botao.getAttribute("aria-expanded") === "true";

      botao.setAttribute("aria-expanded", String(!aberto));
      painel.hidden = aberto;

      $("nutrition-toggle-text").textContent = aberto
          ? "Ver resumo nutricional"
          : "Recolher resumo nutricional";
  });


  $("diary-date").addEventListener("change", async () => {
    mostrarDiario();
    await carregarResumoDoDia();
  });
}

document.addEventListener("DOMContentLoaded", () => {

  if (!sessionStorage.getItem("calunoAuth")) {
        window.location.replace("login.html");
        return;
  }

  const nomeUsuario = sessionStorage.getItem("calunoNome");

  if (nomeUsuario) {
      $("account-label").textContent = nomeUsuario;
      $("account-avatar").textContent = nomeUsuario.charAt(0).toUpperCase();
  }

  $("diary-date").value = hojeEmTexto();
  atualizarIdiomaBusca();
  ligarEventos();
  carregarDiario();
});