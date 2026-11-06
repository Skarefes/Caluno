// Capturamos os elementos do HTML
const formulario = document.getElementById("cadastro-form");
const campoNome = document.getElementById("nome");
const campoSenha = document.getElementById("senha");
const campoConfirmarSenha = document.getElementById("confirmar-senha");

const mensagem = document.getElementById("cadastro-mensagem");
const botaoCadastro = document.getElementById("cadastro-botao");

// Escutamos o envio do formulário
formulario.addEventListener("submit", async (evento) => {

    // Impede o navegador de recarregar a página
    evento.preventDefault();

    // Pegamos os valores digitados
    const nome = campoNome.value.trim();
    const senha = campoSenha.value;
    const confirmarSenha = campoConfirmarSenha.value;

    // Verificamos se as senhas são iguais
    if (senha !== confirmarSenha) {
        mostrarMensagem("As senhas não coincidem.", true);
        return;
    }

    // Verificação adicional no JavaScript
    if (senha.length < 6) {
        mostrarMensagem("A senha precisa ter pelo menos 6 caracteres.", true);
        return;
    }

    // Criamos o objeto que será enviado ao backend
    const dadosCadastro = {
        nome: nome,
        senha: senha
    };

    try {

        botaoCadastro.disabled = true;
        botaoCadastro.textContent = "Criando conta...";

        mostrarMensagem("");

        // Enviamos os dados para o Spring Boot
        const resposta = await fetch(`${API_BASE_URL}/usuarios/cadastrar`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(dadosCadastro)

        });

        // Se o backend retornar algum erro
        if (!resposta.ok) {

            let detalhe = "";

            try {
                const erro = await resposta.json();
                detalhe = erro.message || erro.detail || "";
            } catch (_) {
                // A resposta pode não conter JSON
            }

            throw new Error(
                detalhe || "Não foi possível realizar o cadastro."
            );
        }

        // Cadastro realizado com sucesso
        mostrarMensagem("Conta criada com sucesso!");

        formulario.reset();

        // Encaminha o usuário para o login
        setTimeout(() => {
            window.location.href = "login.html";
        }, 1500);

    } catch (erro) {

        mostrarMensagem(erro.message, true);

    } finally {

        botaoCadastro.disabled = false;
        botaoCadastro.textContent = "Criar minha conta";

    }

});

// Função para exibir mensagens
function mostrarMensagem(texto, erro = false) {

    mensagem.textContent = texto;

    mensagem.classList.toggle("error", erro);

}
