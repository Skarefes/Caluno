// Se a página está rodando na sua máquina, usa o back local.
// Em qualquer outro endereço (Cloudflare), usa o back do Render.
const API_BASE_URL = ["localhost", "127.0.0.1"].includes(window.location.hostname)
    ? "http://localhost:8080"
    : "https://caluno.onrender.com";