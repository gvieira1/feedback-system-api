

# 📢 API de Feedback Interno para Empresas


## 🎯 Objetivo e Público-Alvo

Esta API tem como objetivo facilitar o envio e gestão de feedbacks dentro de uma organização. Ela permite que funcionários compartilhem sugestões, elogios ou críticas de forma identificada ou anônima, enquanto a equipe de Recursos Humanos pode analisar os dados para promover melhorias internas.

**Público-alvo:** Funcionários e administradores (RH) de empresas que desejam fomentar a comunicação interna e obter insights sobre o clima organizacional.

---
## ✅ Funcionalidades Implementadas

As funcionalidades foram desenvolvidas com base nas seguintes histórias de usuário:
### 🔐 Autenticação e Autorização
1. Cadastro de funcionários com nome, e-mail e senha.
2. Login com retorno de token JWT.
3. Acesso completo aos feedbacks apenas por administradores (RH).
### 💬 Envio de Feedbacks
4. Envio de feedback com conteúdo, setor e opção de anonimato.
5. Visualização de feedbacks enviados pelo usuário (exceto anônimos).
6. Ocultação de autor e conteúdo de feedbacks anônimos.
7. Visualização de todos os feedbacks por administradores, com filtros por setor e data.
8. Exclusão de feedbacks ofensivos por administradores.
### 📊 Consultas e Relatórios
9. Geração de relatórios por período e setor.
10. Exportação de feedbacks para CSV.
11. Agrupamento de feedbacks por tipo (elogio, sugestão, crítica, reclamação).

---
## ⚙️ Instruções de Execução Local

### 🔧 Pré-requisitos

- Java 17+
- Maven 3.8+
- Banco de dados MySQL ou H2 (dependendo da configuração)
- IDE ou terminal com suporte a Spring Boot

### 🛠️ Build e Execução

Execute o código abaixo ou o equivalente na IDE de sua preferência:

```bash
# Clone o repositório
git clone https://github.com/gvieira1/feedback-system-api.git

# Acesse a pasta do projeto
cd feedback-system-api

# Compile o projeto
mvn clean install

# Rode a aplicação
mvn spring-boot:run
````

A aplicação será iniciada em: `http://localhost:8080`

---
## 🔐 Como Obter o Token JWT

1. Cadastre um novo employee em uma requisição `POST` para `api/users/public-register`
2. Faça uma requisição `POST` para `api/auth/authenticate` com um JSON contendo `email` e `senha` cadastrado.
3. Um token JWT será retornado.
4. Use o token no header das próximas requisições:

```
Authorization: Bearer <token>
```

> A autenticação utiliza Spring Security com assinatura JWT baseada em chave pública e privada (RSA).

---
## 🧩 Modelo de Dados e Regras de Validação

### Entidades Principais

- **User**
    
    - `id`, ``userName``, `email`, ``password``, `role` e  lista de `feedbacks`
    - Roles possíveis: `EMPLOYEE`, `ADMIN`
    - Email deve ser único e válido.
        
- **Feedback**

    - `id`, `titulo`, `content`,  `sector`,  `anonymous`, `type`, `author`, `createdAt` e `tags`
    - Campos obrigatórios: `content`, ``titulo``, ``sector`` e ``type``
    - `anonymous`: `true` oculta autor no retorno.
### Regras

- Usuários comuns só acessam os próprios feedbacks (exceto anônimos).
- Administradores têm acesso completo com filtros adicionais.
- Feedbacks anônimos ocultam o nome do autor.
    
---
## 🛡️ Autenticação e Autorização

- **Autenticação:** JWT com chave pública e privada.
    
- **Autorização:**
    
    - `EMPLOYEE` pode criar e visualizar seus próprios feedbacks.
    - `ADMIN` pode visualizar todos, aplicar filtros, excluir e gerar relatórios.

---

## 🧪 Testes Implementados


### ✅ Testes Funcionais

- **Autenticação:**
    
    - Login com credenciais válidas.
        
- **Feedback (usuário autenticado):**
    
    - Envio de feedbacks válidos.
    - Retorno 400 para dados inválidos.
    - Acesso negado sem autenticação.
        
- **Feedback (admin):**
    
    - Visualização geral e filtrada.
    - Exclusão com diferentes cenários (sucesso, inexistente, sem permissão).
    - Agrupamento por tipo.
    - Exportação (CSV).
    - Relatórios: top setores, últimos três meses, dashboard.
        
### 🔍 Testes Unitários

- **FeedbackService:**
    
    - Filtragem de feedbacks não anônimos.
    - Criação com dados válidos/inválidos.
    - Agrupamento por tipo.
    - Conversão para DTO respeitando anonimato.
    - Exclusão e tratamento de erros.

---

## 🧪 Formas de Testar a API


### 📘 Swagger (OpenAPI)

Acesse a documentação interativa da API em:

```
http://localhost:8080/swagger-ui.html
```

### 🧪 Postman

1. Importe a collection Postman (arquivo `postman_collection.json`).
    
2. Configure o token JWT como variável global.
    
3. Teste todos os endpoints disponíveis: autenticação, envio de feedback, filtros e relatórios.
    
---
## 🚀 Deploy

> Em construção...

---
