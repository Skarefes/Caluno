async function chamarApi(caminho, opcoes = {}) {

    const credencial = sessionStorage.getItem("calunoAuth");

    // Se não existe credencial, volta para o login
    if (!credencial) {
        window.location.replace("login.html");
        throw new Error("Usuário não autenticado.");
    }

    const resposta = await fetch(`${API_BASE_URL}${caminho}`, {

        ...opcoes,

        headers: {
            "Content-Type": "application/json",
            "Authorization": credencial,
            ...(opcoes.headers || {})
        }

    });

    if (resposta.status === 401) {

        sessionStorage.removeItem("calunoAuth");
        sessionStorage.removeItem("calunoNome");

        window.location.replace("login.html");


        throw new Error("Sua autenticação expirou ou acesso nao foi autorizado.");
    }

    if (!resposta.ok) {

        let detalhe = "";

        try {
            const erro = await resposta.json();
            detalhe = erro.message || erro.detail || "";
        } catch (_) {}

        throw new Error(detalhe || `Erro HTTP ${resposta.status}`);
    }

        // Se o backend retornar 204, não existe conteúdo para converter em JSON

    if (resposta.status === 204) {
        return null;
    }

    return resposta.json();
}
