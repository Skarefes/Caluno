const botaoLogout = document.getElementById("logout-button");

botaoLogout.addEventListener("click", () => {
    sessionStorage.removeItem("calunoAuth");
    sessionStorage.removeItem("calunoNome");

    window.location.replace("login.html");
});