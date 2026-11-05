
const API_BASE_URL = "http://localhost:8080";

const formulario = document.getElementById("login-form");
const campoNome = document.getElementById("nome");
const campoSenha = document.getElementById("senha");
const mensagem = document.getElementById("login-mensagem");

formulario.addEventListener("submit", async (evento) => {

    evento.preventDefault();

    const nome = campoNome.value.trim();
    const senha = campoSenha.value;

    try {

        mostrarMensagem("");

        const credencial = codificarBase64UTF8(`${nome}:${senha}`);

        const resposta = await fetch(`${API_BASE_URL}/auth/login`, {

            method: "GET",

            headers: {
                "Authorization": `Basic ${credencial}`
            }

        });

        if (!resposta.ok) {
            throw new Error("Nome de usuário ou senha incorretos.");
        }

        //Esperando a resposta do backend
        const usuario = await resposta.json();

        // Guarda a credencial durante a sessão da aba
        sessionStorage.setItem("calunoAuth",
            `Basic ${credencial}`);
        sessionStorage.setItem("calunoNome", usuario.nome);

        // Encaminha para o diário
        window.location.href = "index.html";

    } catch (erro) {

        mostrarMensagem(erro.message, true);

    }

});

function mostrarMensagem(texto, erro = false) {

    mensagem.textContent = texto;

    mensagem.classList.toggle("error", erro);

}

function codificarBase64UTF8(texto) {

    const bytes = new TextEncoder().encode(texto);

    let binario = "";

    bytes.forEach(byte => {
        binario += String.fromCharCode(byte);
    });

    return btoa(binario);
}