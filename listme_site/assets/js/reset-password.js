document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('resetPasswordForm');
  const tokenInput = document.getElementById('token');
  const newPasswordInput = document.getElementById('newPassword');
  const confirmPasswordInput = document.getElementById('confirmPassword');
  const messageArea = document.getElementById('messageArea');
  const submitButton = form.querySelector('button[type="submit"]');

  // 1. Pegar o token da URL
  const urlParams = new URLSearchParams(window.location.search);
  const tokenFromUrl = urlParams.get('token');

  if (tokenFromUrl) {
    tokenInput.value = tokenFromUrl;
  } else {
    showMessage('Token de redefinição inválido ou ausente. Por favor, solicite um novo link.', 'error');
    if(submitButton) submitButton.disabled = true;
    return;
  }

  form.addEventListener('submit', async function (event) {
    event.preventDefault();
    hideMessage();

    const token = tokenInput.value;
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;

    // 2. Validação no frontend
    if (newPassword.length < 6) {
      showMessage('A nova senha deve ter pelo menos 6 caracteres.', 'error');
      return;
    }
    // Regex de complexidade (mesma do seu backend)
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@#$%^&+=!]).{6,}$/;
    if (!passwordPattern.test(newPassword)) {
      showMessage('Senha inválida. Deve conter letra, número e um caractere especial (@#$%^&+=!).', 'error');
      return;
    }
    if (newPassword !== confirmPassword) {
      showMessage('As senhas não coincidem.', 'error');
      return;
    }

    if(submitButton) {
      submitButton.disabled = true;
      submitButton.textContent = 'Redefinindo...';
    }

    const backendApiUrl = 'https://listmeapp.tech/api/auth/reset-password'; // SUA URL DE API
    const queryParams = new URLSearchParams({
      token: token,
      password: newPassword,
      confirmPassword: confirmPassword
    });

    try {
      const response = await fetch(`${backendApiUrl}?${queryParams.toString()}`, {
        method: 'POST',
        // Não são necessários headers 'Content-Type' ou 'Accept' para esta requisição POST
        // com query parameters e esperando JSON.
      });

      const resultText = await response.text(); // Ler como texto primeiro
      let resultJson;
      try {
        resultJson = JSON.parse(resultText); // Tentar parsear como JSON
      } catch (e) {
        // Se não for JSON, pode ser um erro HTML do servidor ou proxy
        console.error("Resposta não é JSON:", resultText);
        showMessage(response.ok ? "Operação concluída, mas resposta inesperada." : `Erro ${response.status}: Falha ao conectar com o servidor.`, response.ok ? 'success' : 'error');
        return;
      }


      if (response.ok) {
        showMessage(resultJson.message || 'Senha redefinida com sucesso! Você já pode tentar fazer login.', 'success');
        form.reset();
        if(submitButton) submitButton.textContent = 'Senha Redefinida!';
        // Opcional: setTimeout(() => { window.location.href = 'URL_DA_SUA_TELA_DE_LOGIN'; }, 5000);
      } else {
        showMessage(resultJson.message || `Erro ${response.status}: ${resultJson.error || 'Não foi possível redefinir a senha.'}`, 'error');
      }
    } catch (error) {
      console.error('Erro na requisição de fetch:', error);
      showMessage('Erro de comunicação com o servidor. Verifique sua conexão e tente novamente.', 'error');
    } finally {
      if(submitButton && submitButton.textContent !== 'Senha Redefinida!') {
        submitButton.disabled = false;
        submitButton.textContent = 'Redefinir Senha';
      }
    }
  });

  function showMessage(msg, type) {
    messageArea.textContent = msg;
    messageArea.className = 'message ' + type;
    messageArea.classList.remove('hidden');
  }
  function hideMessage() {
    messageArea.textContent = '';
    messageArea.classList.add('hidden');
    messageArea.className = 'message hidden';
  }
});