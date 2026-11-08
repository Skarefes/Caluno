const formulario = document.getElementById("login-form");
const campoNome = document.getElementById("nome");
const campoSenha = document.getElementById("senha");
const mensagem = document.getElementById("login-mensagem");
const botaoEntrar = document.getElementById("login-botao");
const caixaCarregando = document.getElementById("login-carregando");
const textoCarregando = document.getElementById("login-carregando-texto");

// Depois de 90 segundos desistimos, para a tela não ficar esperando para sempre
const TEMPO_MAXIMO_MS = 90000;

// "Cutuca" o servidor assim que a página abre. Enquanto o usuário digita o nome e a senha,
// o Render já vai acordando. O no-cors evita erro de CORS, porque não precisamos da resposta.
fetch(`${API_BASE_URL}/auth/login`, { mode: "no-cors" }).catch(() => {});

formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const nome = campoNome.value.trim();
    const senha = campoSenha.value;

    mostrarMensagem("");
    mostrarCarregando(true);
    const avisos = agendarAvisos();

    // AbortController cancela o fetch se passar do tempo máximo
    const controle = new AbortController();
    const limite = setTimeout(() => controle.abort(), TEMPO_MAXIMO_MS);

    try {
        const credencial = codificarBase64UTF8(`${nome}:${senha}`);

        const resposta = await fetch(`${API_BASE_URL}/auth/login`, {
            method: "GET",
            headers: { "Authorization": `Basic ${credencial}` },
            signal: controle.signal
        });

        // 401 = o servidor respondeu e a senha está errada. Outros erros são problema do servidor.
        if (resposta.status === 401) {
            throw new Error("Nome de usuário ou senha incorretos.");
        }
        if (!resposta.ok) {
            throw new Error("O servidor não conseguiu responder agora. Tente novamente em instantes.");
        }

        const usuario = await resposta.json();

        sessionStorage.setItem("calunoAuth", `Basic ${credencial}`);
        sessionStorage.setItem("calunoNome", usuario.nome);

        window.location.href = "index.html";

    } catch (erro) {
        if (erro.name === "AbortError") {
            mostrarMensagem("O servidor demorou demais para responder. Tente novamente.", true);
        } else if (erro instanceof TypeError) {
            // fetch falhou antes de ter resposta: servidor ainda acordando ou sem internet
            mostrarMensagem("O servidor ainda está acordando. Aguarde alguns segundos e tente de novo.", true);
        } else {
            mostrarMensagem(erro.message, true);
        }
    } finally {
        // finally roda sempre, com sucesso ou erro: garante que o loading some
        clearTimeout(limite);
        avisos.forEach(clearTimeout);
        mostrarCarregando(false);
    }
});

/* ---------- Funções de apoio ---------- */

function mostrarCarregando(ativo) {
    caixaCarregando.hidden = !ativo;
    botaoEntrar.disabled = ativo;               // impede clicar duas vezes
    botaoEntrar.textContent = ativo ? "Entrando..." : "Entrar";
}

// Troca o texto do aviso conforme o tempo passa, para o usuário saber que não travou
function agendarAvisos() {
    textoCarregando.textContent = "Conectando ao servidor...";
    return [
        setTimeout(() => {
            textoCarregando.textContent =
                "O servidor estava dormindo e está acordando. Isso pode levar até 1 minuto, espere um pouco...";
        }, 4000),
        setTimeout(() => {
            textoCarregando.textContent = "Quase lá! Só mais um instante...";
        }, 30000)
    ];
}

function mostrarMensagem(texto, erro = false) {
    mensagem.textContent = texto;
    mensagem.classList.toggle("error", erro);
}

function codificarBase64UTF8(texto) {
    const bytes = new TextEncoder().encode(texto);
    let binario = "";
    bytes.forEach(byte => { binario += String.fromCharCode(byte); });
    return btoa(binario);
}