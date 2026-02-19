# 🧪 Guia Rápido de Teste - Autenticação JWT

**Para testar rapidamente o fluxo de autenticação usando as collections atualizadas**

---

## 🚀 Teste Rápido - Postman (Recomendado)

### **Passo 1: Importar Collection**
1. Abra o Postman
2. Clique em "Import"
3. Selecione o arquivo: `src/main/resources/postman_collection.json`

### **Passo 2: Criar Usuário (Registro)**
```
Endpoint: POST /api/users
Body:
{
  "name": "Teste Silva",
  "email": "teste@example.com",
  "password": "senha123456"
}
```
**Resposta esperada:** 201 Created com dados do usuário

### **Passo 3: Fazer Login**
```
Endpoint: POST /api/v1/auth/login
Body:
{
  "email": "teste@example.com",
  "password": "senha123456"
}
```
**Resposta esperada:** 200 OK com tokens
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "name": "Teste Silva",
    "email": "teste@example.com"
  }
}
```
✨ **Os tokens são salvos automaticamente nas variáveis da collection!**

### **Passo 4: Criar uma Nota (Testando Token)**
```
Endpoint: POST /api/notes
Body:
{
  "title": "Minha Primeira Nota",
  "content": "Teste de autenticação JWT"
}
```
**O token é incluído automaticamente!**  
**Resposta esperada:** 201 Created com dados da nota

### **Passo 5: Listar Notas**
```
Endpoint: GET /api/notes
```
**Resposta esperada:** 200 OK com lista de notas

### **Passo 6: Renovar Token (Opcional)**
```
Endpoint: POST /api/v1/auth/refresh
Body usa automaticamente {{refreshToken}}
```
**Resposta esperada:** 200 OK com novos tokens

### **Passo 7: Logout**
```
Endpoint: POST /api/v1/auth/logout
Body usa automaticamente {{refreshToken}}
```
**Resposta esperada:** 200 OK
✨ **Os tokens são limpos automaticamente!**

---

## 🧪 Teste Rápido - Insomnia

### **Passo 1: Importar Collection**
1. Abra o Insomnia
2. Clique em "Import/Export"
3. Selecione "Import Data" > "From File"
4. Selecione: `src/main/resources/insomnia_collection.json`

### **Passo 2: Criar Usuário**
```
Endpoint: Users > Create User (Register)
Body:
{
  "name": "Teste Silva",
  "email": "teste@example.com",
  "password": "senha123456"
}
```

### **Passo 3: Fazer Login e Copiar Token**
```
Endpoint: Authentication > Login
Body:
{
  "email": "teste@example.com",
  "password": "senha123456"
}
```
📋 **COPIE o `accessToken` da resposta!**

### **Passo 4: Configurar Token**
Para cada endpoint protegido:
1. Vá ao endpoint (ex: Notes > Create Note)
2. Encontre o header `Authorization`
3. Substitua `COLE_SEU_ACCESS_TOKEN_AQUI` pelo token copiado
4. Formato final: `Bearer eyJhbGc...`

### **Passo 5: Criar uma Nota**
```
Endpoint: Notes > Create Note
Header: Authorization: Bearer {seu_token}
Body:
{
  "title": "Minha Primeira Nota",
  "content": "Teste de autenticação JWT"
}
```

---

## 🎯 Testes Essenciais

### ✅ **Teste 1: Login com Credenciais Válidas**
- **Endpoint:** POST /api/v1/auth/login
- **Esperado:** 200 OK + tokens
- **Verifica:** Autenticação básica funciona

### ✅ **Teste 2: Login com Credenciais Inválidas**
- **Endpoint:** POST /api/v1/auth/login
- **Email/Senha:** Errados
- **Esperado:** 401 Unauthorized
- **Verifica:** Proteção contra credenciais inválidas

### ✅ **Teste 3: Acessar Endpoint Protegido COM Token**
- **Endpoint:** GET /api/notes
- **Header:** Authorization: Bearer {token}
- **Esperado:** 200 OK + dados
- **Verifica:** Autorização funciona

### ✅ **Teste 4: Acessar Endpoint Protegido SEM Token**
- **Endpoint:** GET /api/notes
- **Header:** SEM Authorization
- **Esperado:** 401 Unauthorized
- **Verifica:** Endpoints estão protegidos

### ✅ **Teste 5: Acessar com Token Inválido**
- **Endpoint:** GET /api/notes
- **Header:** Authorization: Bearer token_invalido
- **Esperado:** 401 Unauthorized
- **Verifica:** Validação de token funciona

### ✅ **Teste 6: Refresh Token**
- **Endpoint:** POST /api/v1/auth/refresh
- **Body:** { "refreshToken": "{refresh_token}" }
- **Esperado:** 200 OK + novos tokens
- **Verifica:** Renovação de token funciona

### ✅ **Teste 7: Logout**
- **Endpoint:** POST /api/v1/auth/logout
- **Body:** { "refreshToken": "{refresh_token}" }
- **Esperado:** 200 OK
- **Verifica:** Revogação de token funciona

### ✅ **Teste 8: Usar Refresh Token Após Logout**
- **Endpoint:** POST /api/v1/auth/refresh
- **Body:** { "refreshToken": "{refresh_token_revogado}" }
- **Esperado:** 404 Not Found
- **Verifica:** Token revogado não pode ser usado

---

## 🐛 Troubleshooting

### ❌ Problema: "401 Unauthorized"
**Causas possíveis:**
- Token não fornecido
- Token expirado (após 1 hora)
- Token inválido ou corrompido

**Solução:**
1. Faça login novamente
2. Copie o novo token
3. Use nos endpoints

### ❌ Problema: "Token não é incluído automaticamente" (Postman)
**Solução:**
1. Verifique se executou o Login
2. Vá em "Variables" da collection
3. Confirme que `accessToken` tem valor
4. Nos endpoints, verifique header: `Bearer {{accessToken}}`

### ❌ Problema: "Cannot read property 'accessToken'" (Postman)
**Solução:**
1. Delete a collection
2. Re-importe o arquivo JSON
3. Execute Login novamente

### ❌ Problema: "Refresh token not found"
**Causas:**
- Token já foi revogado (logout)
- Token nunca existiu no banco
- Token expirado (após 7 dias)

**Solução:**
1. Faça login novamente
2. Use o novo refresh token

---

## 📊 Checklist de Validação

Após atualizar as collections, verifique:

- [ ] Collection importada com sucesso
- [ ] Variáveis `accessToken` e `refreshToken` existem (Postman)
- [ ] Variável `baseUrl` configurada (Insomnia)
- [ ] Pasta "Authentication" visível com 3 endpoints
- [ ] Todos os endpoints de Notes têm header `Authorization`
- [ ] Todos os endpoints de Users (exceto POST) têm header `Authorization`
- [ ] Login retorna tokens válidos
- [ ] Tokens são salvos automaticamente (Postman)
- [ ] Endpoints protegidos aceitam token válido
- [ ] Endpoints protegidos rejeitam requisições sem token
- [ ] Refresh token renova tokens corretamente
- [ ] Logout revoga refresh token

---

## 🎓 Dicas Pro

### **Postman**
1. Use **Environment Variables** para múltiplos ambientes (dev, staging, prod)
2. Configure **Pre-request Scripts** para renovar token automaticamente quando expirado
3. Use **Tests** para validar respostas automaticamente

### **Insomnia**
1. Crie **Environments** (Dev, Prod) com diferentes baseUrls
2. Use **Template Tags** para variáveis dinâmicas
3. Configure **Response > Extract Value** para salvar tokens automaticamente

### **Ambos**
1. **Organize Folders:** Agrupe endpoints relacionados
2. **Documente:** Adicione descrições em cada endpoint
3. **Versionamento:** Mantenha collections no Git
4. **Compartilhe:** Exporte e compartilhe com o time

---

## 📚 Próximos Passos

Após validar a autenticação:

1. ✅ Testar todos os endpoints de Notes
2. ✅ Testar todos os endpoints de Users
3. ✅ Verificar tratamento de erros
4. ✅ Testar expiração de tokens
5. ✅ Implementar testes automatizados
6. 🔄 **Próxima fase:** Autorização (controle de acesso por usuário)

---

**Última Atualização:** 05/02/2026  
**Status:** ✅ Pronto para testes
