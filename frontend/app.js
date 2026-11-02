/*
 * Caluno — integração inicial com a API Spring Boot.
 *
 * IMPORTANTE:
 * 1) Ajuste API_BASE_URL para o endereço do seu backend.
 * 2) Confira os nomes dos campos dos DTOs em buildConsumptionPayload()
 *    e nos normalizadores abaixo. Os DTOs não foram enviados junto com os controllers.
 * 3) Os endpoints de /consumos usam Authentication. Este arquivo envia cookies
 *    (credentials: "include"). Quando implementar JWT, configure getAuthHeaders().
 */

const API_BASE_URL = "http://localhost:8080";

const $ = (id) => document.getElementById(id);
const numberFormat = new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 });
const state = {
  selectedFood: null,
  selectedSavedFood: null,
  searchResults: [],
  diaryRecords: [],
  searchTimer: null,
  searchController: null
};

function showMessage(message, isError = false, target = "global-message") {
  const element = $(target);
  element.textContent = message;
  element.classList.toggle("error", isError);
}

function formatNumber(value) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? numberFormat.format(parsed) : "—";
}

function getAuthHeaders() {
  // Futuramente, se usar JWT:
  // const token = localStorage.getItem("token");
  // return token ? { Authorization: `Bearer ${token}` } : {};
  return {};
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...getAuthHeaders(),
      ...(options.headers || {})
    },
    credentials: "include"
  });

  if (!response.ok) {
    let detail = "";
    try {
      const body = await response.json();
      detail = body.message || body.error || "";
    } catch (_) {}
    throw new Error(detail || `Erro HTTP ${response.status}`);
  }

  if (response.status === 204) return null;
  const contentType = response.headers.get("content-type") || "";
  return contentType.includes("application/json") ? response.json() : null;
}

/* Normaliza variações comuns de nomes que podem vir do DTO ou da USDA. */
function firstValue(object, keys, fallback = null) {
  for (const key of keys) {
    if (object && object[key] !== undefined && object[key] !== null) return object[key];
  }
  return fallback;
}

function getFoodId(food) {
  return firstValue(food, ["fdcId", "fdcID", "fdc_id", "id"]);
}

function getFoodName(food) {
  return firstValue(food, ["description", "nome", "name", "foodName", "descricao"], "Alimento sem nome");
}

function getNutrient(food, names) {
  // Pode ser um DTO já achatado...
  const direct = firstValue(food, names);
  if (direct !== null) return Number(direct) || 0;

  // ...ou o formato mais comum da USDA: foodNutrients: [{ nutrient: {name}, amount }]
  const nutrients = firstValue(food, ["foodNutrients", "nutrientes", "nutrients"], []);
  if (!Array.isArray(nutrients)) return 0;

  const normalizedNames = names.map((name) => name.toLowerCase());
  const item = nutrients.find((nutrient) => {
    const label = String(
      firstValue(nutrient.nutrient || nutrient, ["name", "nome", "nutrientName"], "")
    ).toLowerCase();
    return normalizedNames.some((name) => label.includes(name.toLowerCase()));
  });
  return Number(firstValue(item, ["amount", "value", "quantidade"], 0)) || 0;
}

function getFoodNutrition(food) {
  return {
    calories: getNutrient(food, ["calorias", "calories", "kcal", "energy", "valorEnergetico"]),
    protein: getNutrient(food, ["proteinas", "proteina", "protein"]),
    carbs: getNutrient(food, ["carboidratos", "carbohydrate", "carbs"]),
    fat: getNutrient(food, ["gorduras", "gordura", "fat", "totalLipid"])
  };
}

function normalizeDiaryRecord(record) {
  const food = firstValue(record, ["alimento", "food"], {});
  return {
    id: firstValue(record, ["id", "consumoId", "registroId"]),
    name: firstValue(record, ["nomeAlimento", "foodName", "nome", "alimentoNome"], getFoodName(food)),
    quantity: Number(firstValue(record, ["quantidade", "quantity", "porcao"], 0)),
    unit: firstValue(record, ["unidade", "unit"], "g"),
    calories: Number(firstValue(record, ["calorias", "totalCalorias", "caloriasTotais", "totalCalories"], 0)),
    date: firstValue(record, ["data", "dataConsumo", "createdAt", "dataHora"], null)
  };
}

function dateToLocalInput(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function initializeDate() {
  $("diary-date").value = dateToLocalInput(new Date());
  updateDateLabel();
}

function updateDateLabel() {
  const selectedDate = $("diary-date").value;
  if (!selectedDate) return;
  const [year, month, day] = selectedDate.split("-");
  $("summary-date").textContent = `Resumo de ${day}/${month}/${year}`;
}

/* --------------------------- Pesquisa USDA --------------------------- */

async function searchFoods(query) {
  if (state.searchController) state.searchController.abort();
  state.searchController = new AbortController();

  const results = $("search-results");
  const stateBox = $("search-state");
  results.replaceChildren();

  if (query.trim().length < 2) {
    stateBox.hidden = false;
    stateBox.innerHTML = '<span class="state-icon" aria-hidden="true">⌕</span><strong>Continue digitando</strong><span>Informe pelo menos 2 caracteres para pesquisar.</span>';
    $("results-count").textContent = "";
    return;
  }

  stateBox.hidden = false;
  stateBox.innerHTML = '<span class="loader" aria-hidden="true"></span><span>Pesquisando alimentos...</span>';

  try {
    const data = await fetch(`${API_BASE_URL}/alimentos/buscar?nome=${encodeURIComponent(query.trim())}`, {
      headers: getAuthHeaders(),
      credentials: "include",
      signal: state.searchController.signal
    });
    if (!data.ok) throw new Error(`Erro HTTP ${data.status}`);
    const foods = await data.json();
    state.searchResults = Array.isArray(foods) ? foods : [];
    renderSearchResults();
  } catch (error) {
    if (error.name === "AbortError") return;
    stateBox.hidden = false;
    stateBox.innerHTML = '<strong>Não foi possível pesquisar</strong><span>Verifique se o backend está ativo e tente novamente.</span>';
    showMessage(error.message, true);
  }
}

function renderSearchResults() {
  const holder = $("search-results");
  const stateBox = $("search-state");
  holder.replaceChildren();
  $("results-count").textContent = `${state.searchResults.length} encontrado(s)`;

  if (!state.searchResults.length) {
    stateBox.hidden = false;
    stateBox.innerHTML = '<span class="state-icon" aria-hidden="true">⌕</span><strong>Nenhum alimento encontrado</strong><span>Tente outro termo. A busca da USDA costuma funcionar melhor em inglês.</span>';
    return;
  }

  stateBox.hidden = true;
  state.searchResults.forEach((food) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "food-result";
    button.setAttribute("aria-pressed", String(state.selectedFood && getFoodId(state.selectedFood) === getFoodId(food)));

    const info = document.createElement("span");
    const name = document.createElement("span");
    name.className = "food-result-name";
    name.textContent = getFoodName(food);
    const meta = document.createElement("span");
    meta.className = "food-result-meta";
    meta.textContent = `FDC ID: ${getFoodId(food) ?? "não informado"}`;
    info.append(name, meta);

    const kcal = document.createElement("span");
    kcal.className = "food-result-kcal";
    const calories = getFoodNutrition(food).calories;
    kcal.textContent = calories > 0 ? `${formatNumber(calories)} kcal / 100 g` : "Ver detalhes";

    button.append(info, kcal);
    button.addEventListener("click", () => selectFood(food));
    holder.append(button);
  });
}

async function selectFood(food) {
  const fdcId = getFoodId(food);
  if (!fdcId) {
    showMessage("O resultado não trouxe o FDC ID. Confira o DadosResultadoUSDA.", true);
    return;
  }

  showMessage("Carregando informações nutricionais...", false, "portion-message");
  $("no-selection").hidden = true;
  $("selected-food").hidden = false;
  $("selected-name").textContent = getFoodName(food);
  $("selected-description").textContent = `Código USDA: ${fdcId}`;
  state.selectedFood = food;
  state.selectedSavedFood = null;
  updatePortionPreview();

  try {
    // Busca os detalhes completos na sua API. Se a rota falhar, mantém o resultado da busca.
    const detail = await apiRequest(`/alimentos/usda/${encodeURIComponent(fdcId)}`, { method: "GET" });
    state.selectedFood = { ...food, ...detail };
    $("selected-name").textContent = getFoodName(state.selectedFood);
    updatePortionPreview();
    showMessage("", false, "portion-message");
  } catch (error) {
    // Não impede a seleção; pode ser que a busca já traga os nutrientes.
    showMessage("Não consegui carregar os detalhes completos. A estimativa pode ficar incompleta.", true, "portion-message");
  }
}

/* --------------------------- Porção e consumo --------------------------- */

function updatePortionPreview() {
  if (!state.selectedFood) return;
  const quantity = Number($("portion-quantity").value);
  const nutrition = getFoodNutrition(state.selectedFood);
  const factor = Number.isFinite(quantity) && quantity > 0 ? quantity / 100 : 0;

  $("portion-calories").innerHTML = `${formatNumber(nutrition.calories * factor)} <small>kcal</small>`;
  $("portion-protein").textContent = `Proteínas: ${formatNumber(nutrition.protein * factor)} g`;
  $("portion-carbs").textContent = `Carboidratos: ${formatNumber(nutrition.carbs * factor)} g`;
  $("portion-fat").textContent = `Gorduras: ${formatNumber(nutrition.fat * factor)} g`;
}

/*
 * ADAPTE ESTA FUNÇÃO ao seu record DadosRegistroConsumo.
 * Estou assumindo provisoriamente os campos alimentoId, quantidade e unidade.
 * Se seu record usar, por exemplo, idAlimento ou porcaoGramas, altere aqui.
 */
function buildConsumptionPayload(savedFood, quantity) {
  const savedFoodId = firstValue(savedFood, ["id", "alimentoId"]);
  return {
    alimentoId: savedFoodId,
    quantidade: quantity,
    unidade: "g"
  };
}

async function addFoodToDiary(event) {
  event.preventDefault();
  if (!state.selectedFood) {
    showMessage("Selecione um alimento antes de adicionar.", true, "portion-message");
    return;
  }

  const quantity = Number($("portion-quantity").value);
  if (!Number.isFinite(quantity) || quantity <= 0) {
    showMessage("Informe uma quantidade válida em gramas.", true, "portion-message");
    $("portion-quantity").focus();
    return;
  }

  const fdcId = getFoodId(state.selectedFood);
  const button = $("add-food-button");
  button.disabled = true;
  button.textContent = "Salvando alimento...";

  try {
    // Primeiro salva/recupera o alimento no banco local usando o FDC ID.
    const savedFood = await apiRequest(`/alimentos/salvar/${encodeURIComponent(fdcId)}`, {
      method: "POST"
    });
    state.selectedSavedFood = savedFood;

    // Depois registra o consumo vinculado ao usuário autenticado.
    const payload = buildConsumptionPayload(savedFood, quantity);
    await apiRequest("/consumos/registrar", {
      method: "POST",
      body: JSON.stringify(payload)
    });

    showMessage("Alimento adicionado ao seu diário!", false, "portion-message");
    await loadDiary();
    $("food-search").value = "";
    $("clear-search").hidden = true;
    $("search-results").replaceChildren();
    $("search-state").hidden = false;
    $("search-state").innerHTML = '<span class="state-icon" aria-hidden="true">✓</span><strong>Alimento registrado</strong><span>Você pode pesquisar outro alimento.</span>';
    $("results-count").textContent = "";
    state.selectedFood = null;
    state.selectedSavedFood = null;
    $("selected-food").hidden = true;
    $("no-selection").hidden = false;
    $("portion-quantity").value = "100";
  } catch (error) {
    showMessage(`Não foi possível registrar: ${error.message}`, true, "portion-message");
  } finally {
    button.disabled = false;
    button.innerHTML = '<span aria-hidden="true">＋</span> Adicionar ao meu diário';
  }
}

/* --------------------------- Diário --------------------------- */

async function loadDiary() {
  $("diary-loading").hidden = false;
  $("diary-empty").hidden = true;
  $("diary-error").hidden = true;
  $("diary-list").replaceChildren();

  try {
    const records = await apiRequest("/consumos/listar-tudo", { method: "GET" });
    state.diaryRecords = Array.isArray(records) ? records.map(normalizeDiaryRecord) : [];
    renderDiary();
    await loadDailySummary();
  } catch (error) {
    $("diary-loading").hidden = true;
    $("diary-error").hidden = false;
    showMessage(`Erro ao carregar o diário: ${error.message}`, true);
  }
}

async function loadDailySummary() {
  const date = $("diary-date").value;
  const query = date ? `?data=${encodeURIComponent(date)}` : "";
  try {
    const summary = await apiRequest(`/consumos/resumo-diario${query}`, { method: "GET" });
    const calories = firstValue(summary, ["calorias", "totalCalorias", "caloriasTotais", "totalCalories", "energia"], null);
    if (calories !== null) {
      $("daily-calories").textContent = formatNumber(calories);
      $("diary-total-calories").innerHTML = `${formatNumber(calories)} <small>kcal</small>`;
    }
  } catch (_) {
    // A listagem continua funcionando mesmo se o formato do DTO de resumo for diferente.
  }
}

function renderDiary() {
  $("diary-loading").hidden = true;
  const date = $("diary-date").value;
  const selectedDayRecords = state.diaryRecords.filter((record) => {
    if (!record.date) return true;
    return String(record.date).slice(0, 10) === date;
  });

  $("food-count").textContent = formatNumber(selectedDayRecords.length);
  $("diary-empty").hidden = selectedDayRecords.length > 0;
  $("diary-list").replaceChildren();

  const total = selectedDayRecords.reduce((sum, record) => sum + (Number(record.calories) || 0), 0);
  $("diary-total-calories").innerHTML = `${formatNumber(total)} <small>kcal</small>`;
  $("daily-calories").textContent = formatNumber(total);

  selectedDayRecords.forEach((record) => {
    const row = document.createElement("article");
    row.className = "diary-entry";

    const symbol = document.createElement("span");
    symbol.className = "entry-symbol";
    symbol.textContent = "◒";

    const info = document.createElement("div");
    info.className = "entry-info";
    const name = document.createElement("strong");
    name.textContent = record.name || "Alimento";
    const details = document.createElement("span");
    details.textContent = `${formatNumber(record.quantity)} ${record.unit || "g"}`;
    info.append(name, details);

    const calories = document.createElement("strong");
    calories.className = "entry-calories";
    calories.textContent = `${formatNumber(record.calories)} kcal`;

    const remove = document.createElement("button");
    remove.className = "delete-entry";
    remove.type = "button";
    remove.textContent = "×";
    remove.setAttribute("aria-label", `Excluir ${record.name}`);
    remove.addEventListener("click", () => deleteDiaryEntry(record.id, remove));

    row.append(symbol, info, calories, remove);
    $("diary-list").append(row);
  });
}

async function deleteDiaryEntry(id, button) {
  if (!id) {
    showMessage("O registro não trouxe um ID válido. Confira DadosDetalhamentoConsumo.", true);
    return;
  }
  if (!confirm("Deseja excluir este alimento do seu diário?")) return;

  button.disabled = true;
  try {
    await apiRequest(`/consumos/${encodeURIComponent(id)}`, { method: "DELETE" });
    showMessage("Registro removido do diário.");
    await loadDiary();
  } catch (error) {
    showMessage(`Não foi possível excluir: ${error.message}`, true);
    button.disabled = false;
  }
}

/* --------------------------- Eventos --------------------------- */

function bindEvents() {
  $("food-search").addEventListener("input", () => {
    const query = $("food-search").value.trim();
    $("clear-search").hidden = query.length === 0;
    clearTimeout(state.searchTimer);
    state.searchTimer = setTimeout(() => searchFoods(query), 450);
  });

  $("clear-search").addEventListener("click", () => {
    $("food-search").value = "";
    $("clear-search").hidden = true;
    if (state.searchController) state.searchController.abort();
    state.searchResults = [];
    $("search-results").replaceChildren();
    $("search-state").hidden = false;
    $("search-state").innerHTML = '<span class="state-icon" aria-hidden="true">⌕</span><strong>Comece pesquisando</strong><span>Os alimentos encontrados aparecerão aqui.</span>';
    $("results-count").textContent = "";
  });

  $("portion-quantity").addEventListener("input", updatePortionPreview);
  $("portion-form").addEventListener("submit", addFoodToDiary);
  $("change-food").addEventListener("click", () => {
    $("food-search").focus();
    $("food-search").scrollIntoView({ behavior: "smooth", block: "center" });
  });
  $("diary-date").addEventListener("change", async () => {
    updateDateLabel();
    await loadDailySummary();
    renderDiary();
  });
  $("retry-diary").addEventListener("click", loadDiary);
}

document.addEventListener("DOMContentLoaded", () => {
  initializeDate();
  bindEvents();
  loadDiary();
});
