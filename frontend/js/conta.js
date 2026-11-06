document.addEventListener("DOMContentLoaded", () => {

    // 1. Verifica se existe autenticação
    const credencial = sessionStorage.getItem("calunoAuth");

    if (!credencial) {
        window.location.replace("login.html");
        return;
    }

    // 2. Elementos da página
    const nomeUsuario = sessionStorage.getItem("calunoNome");

    const avatar = document.getElementById("account-avatar");
    const nome = document.getElementById("account-username");

    const modal = document.getElementById("delete-modal");
    const botaoAbrirModal = document.getElementById("open-delete-modal");
    const botaoCancelar = document.getElementById("cancel-delete");
    const botaoConfirmar = document.getElementById("confirm-delete");

    const mensagem = document.getElementById("account-message");
    const erroModal = document.getElementById("modal-error");

    // Mostra os dados do usuário
    if (nomeUsuario) {
        nome.textContent = nomeUsuario;
        avatar.textContent = nomeUsuario.charAt(0).toUpperCase();
    } else {
        nome.textContent = "Usuário";
    }

    // 3. Abre a confirmação
    botaoAbrirModal.addEventListener("click", () => {
        erroModal.textContent = "";
        modal.hidden = false;
        botaoConfirmar.focus();
    });

    // Fecha a confirmação
    function fecharModal() {
        modal.hidden = true;
        botaoAbrirModal.focus();
    }

    botaoCancelar.addEventListener("click", fecharModal);

    // Fecha ao clicar fora da caixa
    modal.addEventListener("click", (evento) => {
        if (evento.target === modal) {
            fecharModal();
        }
    });

    // Fecha ao pressionar ESC
    document.addEventListener("keydown", (evento) => {
        if (evento.key === "Escape" && !modal.hidden) {
            fecharModal();
        }
    });

    // 4. Exclui a conta
    botaoConfirmar.addEventListener("click", async () => {

        botaoConfirmar.disabled = true;
        botaoConfirmar.textContent = "Excluindo...";
        erroModal.textContent = "";

        try {
            await chamarApi("/usuarios/me", {
                method: "DELETE"
            });

            // Exclusão realizada
            sessionStorage.removeItem("calunoAuth");
            sessionStorage.removeItem("calunoNome");

            window.location.replace("cadastro.html");

        } catch (erro) {

            erroModal.textContent =
                erro.message || "Não foi possível excluir sua conta.";

            botaoConfirmar.disabled = false;
            botaoConfirmar.textContent = "Sim, excluir conta";
        }
    });

});
