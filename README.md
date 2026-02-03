# Notes API

API RESTful para gerenciamento de notas, desenvolvida com Kotlin, Spring Boot e PostgreSQL.

## 📚 Documentação do Projeto

- 📖 [**README.md**](README.md) - Este arquivo (visão geral)
- 🔄 [**RESTFUL_API.md**](RESTFUL_API.md) - Explicação sobre APIs RESTful e análise de aderência do projeto
- 🐳 [**DOCKER.md**](DOCKER.md) - Guia completo de Docker e Docker Compose
- 🚀 [**QUICKSTART.md**](QUICKSTART.md) - Guia rápido de início
- 👨‍💻 [**DESENVOLVIMENTO.md**](DESENVOLVIMENTO.md) - Guia completo de desenvolvimento
- 🏗️ [**ARQUITETURA.md**](ARQUITETURA.md) - Diagramas e padrões arquiteturais
- 📦 [**ESTRUTURA.md**](ESTRUTURA.md) - Estrutura completa do projeto
- ✅ [**SETUP_COMPLETO.md**](SETUP_COMPLETO.md) - Detalhes da configuração inicial
- 🎉 [**CONCLUSAO.md**](CONCLUSAO.md) - Status final e resumo completo
- 🔌 [**API_COLLECTIONS_README.md**](src/main/resources/API_COLLECTIONS_README.md) - Guia das collections Postman/Insomnia

---

## ⚡ Quick Start

### Opção 1: Script Automático (Recomendado) 🎯
```bash
# Cria .env e inicia tudo automaticamente
cp .env.example .env
./start.sh
```

### Opção 2: Comandos Manuais 🔧
```bash
# 1. Criar arquivo de configuração
cp .env.example .env

# 2. Iniciar containers Docker
docker-compose up -d

# 3. Executar a aplicação
./gradlew bootRun
```

### 3. Acessar a API
- **API Base**: http://localhost:8080/api/notes
- **pgAdmin**: http://localhost:5050
  - Email: admin@notesapi.com
  - Senha: admin

### Testar
```bash
# Listar notas
curl http://localhost:8080/api/notes

# Criar nota
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"Minha Nota","content":"Conteúdo"}'
```

### Parar o Ambiente
```bash
./stop.sh
```

---

## 🚀 Tecnologias

- Kotlin 2.2.20
- Spring Boot 3.2.2
- Spring Data JPA
- PostgreSQL 15
- Docker & Docker Compose
- Gradle
- Java 21

## 📋 Funcionalidades

- ✅ Criar notas
- ✅ Listar todas as notas
- ✅ Buscar nota por ID
- ✅ Buscar notas por título
- ✅ Atualizar notas
- ✅ Excluir notas
- ✅ Validação de dados
- ✅ Tratamento de exceções

## 🏗️ Estrutura do Projeto

```
src/main/kotlin/dev/andresoares/
├── NotesApiApplication.kt        # Classe principal
├── config/
│   └── WebConfig.kt              # Configurações CORS
├── controller/
│   └── NoteController.kt         # Endpoints REST
├── dto/
│   └── NoteDto.kt                # DTOs para requisições e respostas
├── exception/
│   └── GlobalExceptionHandler.kt # Tratamento global de exceções
├── model/
│   └── Note.kt                   # Entidade JPA
├── repository/
│   └── NoteRepository.kt         # Repositório JPA
└── service/
    └── NoteService.kt            # Lógica de negócio
```

## 🔧 Como Executar

### Pré-requisitos
- Java 21
- Docker e Docker Compose
- Gradle (ou use o wrapper `./gradlew`)

### Passos

1. **Clonar o repositório**

2. **Criar arquivo de configuração**
   ```bash
   cp .env.example .env
   ```
   
   O arquivo `.env` contém as configurações de:
   - PostgreSQL (porta, usuário, senha, database)
   - pgAdmin (porta, credenciais)
   - Aplicação (porta)

3. **Opção A: Usar script automático (Recomendado)**
   ```bash
   ./start.sh
   ```
   
   O script irá:
   - ✅ Verificar se `.env` existe
   - ✅ Iniciar containers Docker
   - ✅ Aguardar PostgreSQL ficar pronto
   - ✅ Iniciar aplicação Spring Boot

4. **Opção B: Manual**
   ```bash
   # Iniciar containers
   docker-compose up -d
   
   # Validar ambiente (opcional)
   ./validate-docker.sh
   
   # Executar aplicação
   ./gradlew bootRun
   ```

5. **A aplicação estará disponível em:** `http://localhost:8080`

### Scripts Disponíveis

| Script | Descrição |
|--------|-----------|
| `./start.sh` | Inicia containers + aplicação |
| `./stop.sh` | Para containers (preserva dados) |
| `./restart.sh` | Reinicia containers |
| `./validate-docker.sh` | Valida ambiente Docker |

Para mais detalhes sobre os scripts, consulte [SCRIPTS_GUIDE.md](SCRIPTS_GUIDE.md).

## 📚 Endpoints da API

### Listar todas as notas
```http
GET /api/notes
```

### Buscar notas por título
```http
GET /api/notes?title=exemplo
```

### Buscar nota por ID
```http
GET /api/notes/{id}
```

### Criar nova nota
```http
POST /api/notes
Content-Type: application/json

{
  "title": "Título da nota",
  "content": "Conteúdo da nota"
}
```

### Atualizar nota
```http
PUT /api/notes/{id}
Content-Type: application/json

{
  "title": "Novo título",
  "content": "Novo conteúdo"
}
```

### Excluir nota
```http
DELETE /api/notes/{id}
```

## 🐳 Docker e Gerenciamento do Banco de Dados

### Variáveis de Ambiente (.env)

O projeto utiliza variáveis de ambiente definidas no arquivo `.env`:

```env
# PostgreSQL
POSTGRES_DB=notesdb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

# pgAdmin
PGADMIN_DEFAULT_EMAIL=admin@notesapi.com
PGADMIN_DEFAULT_PASSWORD=admin
PGADMIN_PORT=5050

# Aplicação
APP_PORT=8080
```

**Criar arquivo .env:**
```bash
cp .env.example .env
```

### Iniciar os containers
```bash
docker-compose up -d
# Ou use o script
./start.sh
```

### Verificar status dos containers
```bash
docker-compose ps
```

### Ver logs dos containers
```bash
# Todos os serviços
docker-compose logs -f

# Apenas PostgreSQL
docker-compose logs -f postgres

# Apenas pgAdmin
docker-compose logs -f pgadmin
```

### Parar os containers
```bash
docker-compose down
# Ou use o script
./stop.sh
```

### Reiniciar containers
```bash
docker-compose restart
# Ou use o script
./restart.sh
```

### Parar e remover volumes (apaga os dados)
```bash
docker-compose down -v
```

### Acessar pgAdmin

Para gerenciar o banco de dados PostgreSQL através do pgAdmin:

1. Acesse: `http://localhost:5050`
2. Login:
   - Email: `admin@notesapi.com`
   - Senha: `admin`
3. Adicionar servidor PostgreSQL:
   - **General → Name**: NotesDB
   - **Connection → Host**: `postgres` (⚠️ nome do serviço no Docker, não "localhost")
   - **Connection → Port**: `5432`
   - **Connection → Database**: `notesdb`
   - **Connection → Username**: `postgres`
   - **Connection → Password**: `postgres`

### Conectar direto ao PostgreSQL (CLI)
```bash
docker-compose exec postgres psql -U postgres -d notesdb
```

**Comandos úteis no psql:**
```sql
-- Listar tabelas
\dt

-- Ver estrutura da tabela
\d note

-- Ver dados
SELECT * FROM note;

-- Sair
\q
```

### Backup e Restore

**Fazer backup:**
```bash
docker-compose exec postgres pg_dump -U postgres notesdb > backup.sql
```

**Restaurar backup:**
```bash
docker-compose exec -T postgres psql -U postgres -d notesdb < backup.sql
```

## 🗄️ Configuração do Banco de Dados

O projeto utiliza PostgreSQL como banco de dados. As configurações estão em `application.yml`:

- **Database**: notesdb
- **Host**: localhost
- **Port**: 5432
- **Username**: postgres
- **Password**: postgres

## 📦 Collections para Postman e Insomnia

Collections prontas para importar e testar todas as APIs:

- 📁 **postman_collection.json** - Collection para Postman
- 📁 **insomnia_collection.json** - Collection para Insomnia
- 📄 **API_COLLECTIONS_README.md** - Documentação completa das collections

**Localização:** `src/main/resources/`

### Como Usar:
1. **Postman**: Import → Selecione `postman_collection.json`
2. **Insomnia**: Preferences → Data → Import Data → Selecione `insomnia_collection.json`

Ambas as collections incluem:
- ✅ Todos os 6 endpoints da API
- ✅ Exemplos prontos para usar
- ✅ Variáveis de ambiente configuradas
- ✅ Casos de teste para validação e erros

Para mais detalhes, consulte o arquivo [API_COLLECTIONS_README.md](src/main/resources/API_COLLECTIONS_README.md)

---

## 📝 Exemplo de Uso

### Criar uma nota
```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Minha primeira nota",
    "content": "Conteúdo da minha primeira nota"
  }'
```

### Listar todas as notas
```bash
curl http://localhost:8080/api/notes
```

### Buscar nota por ID
```bash
curl http://localhost:8080/api/notes/1
```

### Atualizar nota
```bash
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Título atualizado",
    "content": "Conteúdo atualizado"
  }'
```

### Excluir nota
```bash
curl -X DELETE http://localhost:8080/api/notes/1
```

## 🎯 Próximos Passos

- [ ] Adicionar autenticação e autorização
- [ ] Implementar paginação
- [ ] Adicionar tags às notas
- [ ] Implementar busca avançada
- [ ] Adicionar suporte a anexos
- [ ] Adicionar documentação Swagger/OpenAPI
- [ ] Implementar testes unitários e de integração
- [ ] Adicionar cache (Redis)
- [ ] Implementar CI/CD

## 📄 Licença

Este projeto está sob a licença MIT.
