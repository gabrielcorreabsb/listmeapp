<p align="center">
<img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/srcgit/logo.png?raw=true" alt="ListMe Logo" width="200"/>
</p>

# ListMe - Gestão Inteligente de Pedidos 🛍️

**ListMe** é um aplicativo mobile projetado para modernizar e automatizar o processo de anotação e gerenciamento de pedidos, trazendo eficiência e organização para o seu negócio.

## 🚧 Status do Projeto: Em Desenvolvimento 🚧

Este projeto está atualmente em desenvolvimento ativo como parte da disciplina "Projeto Integrado em Programação APP". Novas funcionalidades e melhorias estão sendo implementadas continuamente.

## 🎯 Sobre o Projeto

O ListMe nasceu da necessidade de otimizar a gestão comercial, especialmente para empresas que lidam com um volume significativo de pedidos e clientes. Nosso objetivo é:

*   **Digitalizar e agilizar** o processo de tomada de pedidos.
*   **Melhorar a organização** do catálogo de produtos e da base de clientes.
*   **Reduzir erros operacionais** comuns em processos manuais.
*   **Aumentar a agilidade no atendimento**, garantindo maior produtividade e precisão.

## ✨ Funcionalidades

### Implementadas e em Desenvolvimento:
*   **Autenticação de Usuários:** Sistema de login seguro para funcionários e administradores.
*   **Recuperação de Senha:** Funcionalidade de "esqueci minha senha" via email.
*   **Gerenciamento de Funcionários (Admin):** CRUD completo para administradores gerenciarem contas de funcionários.
*   **Gerenciamento de Clientes:**
    *   Listagem, cadastro, edição e exclusão de clientes.
    *   Máscaras de entrada e validações para dados como CNPJ/CPF e telefone.
*   **Gerenciamento de Produtos:**
    *   Listagem, cadastro, edição e exclusão de produtos.
    *   Manuseio de preços com `BigDecimal` e unidades de medida.
*   **Criação de Orçamentos/Pedidos:**
    *   Seleção de cliente.
    *   Adição de múltiplos produtos com quantidades.
    *   Cálculo automático de subtotais e total geral.
    *   Definição de forma de pagamento e observações.
*   **Gerenciamento de Orçamentos/Pedidos:**
    *   Listagem de orçamentos existentes.
    *   Visualização detalhada de orçamentos.
    *   Atualização de status (Pendente, Enviado, Concluído, Cancelado).
    *   Edição e exclusão de orçamentos (com permissões).
*   **Geração de Comprovantes:** Funcionalidade para gerar orçamentos em formato de imagem (PNG) para compartilhamento.

### Planejadas para o Futuro:
*   Geração de orçamentos em formato PDF.
*   Envio direto de orçamentos por email via app.
*   Notificações e lembretes (ex: status de pedido, follow-up de orçamento).
*   Dashboard com estatísticas e relatórios.
*   Integrações com outros sistemas.

## 🛠️ Tecnologias Utilizadas

### Backend
*   **Java 21+**
*   **Spring Boot 3.x**
*   **Spring Security & JWT:** Para autenticação e autorização.
*   **Spring Data JPA (Hibernate):** Para persistência de dados.
*   **PostgreSQL:** Banco de dados relacional.
*   **Maven:** Gerenciador de dependências e build.
*   **Docker & Docker Compose:** Para conteinerização e orquestração do ambiente de backend.
*   **Spring Mail & Jakarta Mail:** Para funcionalidade de envio de emails.

### Frontend Mobile (Android)
*   **Kotlin**
*   **Android Studio**
*   **XML (View System):** Para layouts de interface.
*   **Material Design Components:** Para componentes de UI modernos.
*   **Retrofit & OkHttp:** Para comunicação com a API backend.
*   **Gson:** Para serialização/desserialização de JSON.
*   **Coroutines Kotlin:** Para programação assíncrona.
*   **Android Navigation Component:** Para navegação entre telas/fragments.
*   **Glide (ou Coil):** Para carregamento de imagens.
*   **Bibliotecas de Máscara (ex: Maskara):** Para formatação de campos de entrada.
*   **ThreeTenABP:** Para retrocompatibilidade da API `java.time`.

### Infraestrutura & Deploy
*   **Nginx:** Como proxy reverso.
*   **Let's Encrypt (Certbot):** Para certificados SSL/TLS.
*   Servidor VPS (Ubuntu).

## 🚀 Como Começar (Para Desenvolvedores)

### Pré-requisitos
*   JDK 21+
*   Maven 3.6+
*   PostgreSQL (instalado localmente ou via Docker)
*   Docker e Docker Compose (para rodar o ambiente completo facilmente)
*   Android Studio (última versão estável recomendada)
*   Um emulador Android (API 24+) ou dispositivo físico.

### Configuração do Backend
1.  Clone o repositório: `git clone https://github.com/gabrielcorreabsb/listmeapp.git`
2.  Navegue até a pasta `listme_backend`.
3.  Configure as variáveis de ambiente ou o arquivo `application-dev.properties` para seu banco de dados local e configurações de email.
    *   Crie um banco de dados PostgreSQL chamado `listme_dev` (ou o nome que você usa para desenvolvimento).
4.  Execute o backend:
    *   Via IDE: Execute a classe principal `ApiTesteApplication.java`.
    *   Via Maven: `mvn spring-boot:run`
    *   Via Docker Compose (recomendado para ambiente completo):
        ```bash
        cd caminho/para/listme_backend # Onde está seu Dockerfile e docker-compose.yml
        docker-compose build listme-api # (Construir apenas a API, ou 'docker-compose build' para tudo)
        docker-compose up -d # Inicia todos os serviços em background
        ```

### Configuração do Frontend (Android)
1.  Abra a pasta do projeto Android no Android Studio.
2.  Certifique-se de que a `BASE_URL` no arquivo `RetrofitClient.kt` está apontando para o endereço correto do seu backend:
    *   Para emulador rodando na mesma máquina que o backend Dockerizado: `http://10.0.2.2:PORTA_DO_HOST_DA_API/` (ex: `http://10.0.2.2:8082/`)

3.  Compile e execute o aplicativo no emulador ou dispositivo.

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s1.png?raw=true" alt="ListMe Tela 1" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s2.png?raw=true" alt="ListMe Tela 2" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s3.png?raw=true" alt="ListMe Tela 3" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s4.png?raw=true" alt="ListMe Tela 4" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s5.png?raw=true" alt="ListMe Tela 5" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s6.png?raw=true" alt="ListMe Tela 6" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s7.png?raw=true" alt="ListMe Tela 7" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s8.png?raw=true" alt="ListMe Tela 8" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s9.png?raw=true" alt="ListMe Tela 9" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s10.png?raw=true" alt="ListMe Tela 10" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s11.png?raw=true" alt="ListMe Tela 11" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s12.png?raw=true" alt="ListMe Tela 12" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s13.png?raw=true" alt="ListMe Tela 13" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s14.png?raw=true" alt="ListMe Tela 14" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s15.png?raw=true" alt="ListMe Tela 15" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s16.png?raw=true" alt="ListMe Tela 16" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s17.png?raw=true" alt="ListMe Tela 17" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s18.png?raw=true" alt="ListMe Tela 18" width="250"/>
  <img src="https://github.com/gabrielcorreabsb/listmeapp/blob/main/listme_site/assets/imgs/s19.png?raw=true" alt="ListMe Tela 19" width="250"/>
</p>


## 🤝 Contribuições

No momento, este é um projeto acadêmico com uma equipe definida. No entanto, feedback e sugestões são sempre bem-vindos! Abra uma *issue* para discutir ideias ou reportar bugs.

## 👥 Equipe de Desenvolvimento

*   Ana Clara Silva Fonseca
*   André Luís Araújo Silva
*   Bruno
*   Daniel Castro R. Santos
*   Gabriel Corrêa *(Dev Full Stack)*
*   Jheniffer
*   João Pedro
*   Micael
*   Samuel Costa Braga
*   Vinicius

## 👨‍🏫 Orientador

*   Prof. Paulo Dutra

## 🏢 Cliente Parceiro

*   Maxmel Doces e Embalagens

---
<p align="center">
  ListMe App - Projeto em Desenvolvimento - 2025
</p>
