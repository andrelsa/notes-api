# 🚀 Quick Start - Notes API

## ⚡ Início Rápido (Recomendado)

### Opção 1: Script Automático 🎯
```bash
# Inicia containers Docker + aplicação
./start.sh
```

**O que o script faz:**
- ✅ Verifica arquivo `.env`
- ✅ Inicia PostgreSQL e pgAdmin
- ✅ Aguarda banco ficar pronto
- ✅ Inicia aplicação Spring Boot

### Opção 2: Manual 🔧
```bash
# 1. Criar arquivo .env (primeira vez)
cp .env.example .env

# 2. Iniciar containers Docker
docker-compose up -d

# 3. Executar a aplicação
./gradlew bootRun
```

---

## 🛑 Parar o Ambiente

```bash
# Parar containers (mantém os dados)
./stop.sh

# Ou manualmente
docker-compose down
```

---

## 📦 Outros Comandos Úteis

### Compilar o projeto
```bash
./gradlew build
```

### Executar testes
```bash
./gradlew test
```

### Limpar e compilar
```bash
./gradlew clean build
```

### Reiniciar containers
```bash
./restart.sh
```

### Validar ambiente Docker
```bash
./validate-docker.sh
```

---

## 🌐 URLs e Credenciais

### Aplicação
- **API Base**: http://localhost:8080/api/notes

### Banco de Dados
- **PostgreSQL**: localhost:5432
  - Database: `notesdb`
  - Username: `postgres`
  - Password: `postgres`

### Gerenciamento
- **pgAdmin**: http://localhost:5050
  - Email: `admin@notesapi.com`
  - Password: `admin`

**Configurar servidor no pgAdmin:**
- Host: `postgres` (⚠️ use "postgres", não "localhost")
- Port: `5432`
- Database: `notesdb`
- Username: `postgres`
- Password: `postgres`

---

## 📝 Exemplos de Uso da API

### Listar todas as notas
```bash
curl http://localhost:8080/api/notes
```

### Criar uma nota
```bash
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"Minha Nota","content":"Conteúdo da nota"}'
```

### Buscar nota por ID
```bash
curl http://localhost:8080/api/notes/1
```

### Atualizar nota
```bash
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Título Atualizado","content":"Conteúdo atualizado"}'
```

### Excluir nota
```bash
curl -X DELETE http://localhost:8080/api/notes/1
```

### Buscar por título
```bash
curl "http://localhost:8080/api/notes?title=Minha"
```

---

## 🐳 Comandos Docker

### Ver status dos containers
```bash
docker-compose ps
```

### Ver logs
```bash
# Todos os serviços
docker-compose logs -f

# Apenas PostgreSQL
docker-compose logs -f postgres

# Apenas pgAdmin
docker-compose logs -f pgadmin
```

### Acessar PostgreSQL via CLI
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

### Reiniciar do zero (⚠️ apaga dados)
```bash
docker-compose down -v
docker-compose up -d
```


---

## 📁 Estrutura Simplificada

```
src/main/kotlin/dev/andresoares/
├── NotesApiApplication.kt  # Aplicação principal
├── controller/             # Endpoints REST
├── service/                # Lógica de negócio
├── repository/             # Acesso a dados
├── model/                  # Entidades JPA
├── dto/                    # DTOs
├── exception/              # Tratamento de erros
└── config/                 # Configurações
```

---

## 🎯 Endpoints Disponíveis

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/notes` | Listar todas as notas |
| GET | `/api/notes?title=xxx` | Buscar por título |
| GET | `/api/notes/{id}` | Buscar por ID |
| POST | `/api/notes` | Criar nota |
| PUT | `/api/notes/{id}` | Atualizar nota |
| DELETE | `/api/notes/{id}` | Excluir nota |

---

## 📚 Documentação Completa

### Guias de Configuração
- **[README.md](README.md)** - Visão geral do projeto
- **[DOCKER.md](DOCKER.md)** - Guia completo de Docker
- **[DOCKER_SETUP_GUIDE.md](DOCKER_SETUP_GUIDE.md)** - Configuração passo a passo
- **[SCRIPTS_GUIDE.md](SCRIPTS_GUIDE.md)** - Guia dos scripts de automação

### Guias de Desenvolvimento
- **[DESENVOLVIMENTO.md](DESENVOLVIMENTO.md)** - Guia de desenvolvimento
- **[ARQUITETURA.md](ARQUITETURA.md)** - Diagramas e arquitetura
- **[SETUP_COMPLETO.md](SETUP_COMPLETO.md)** - Detalhes da configuração

### APIs e Testes
- **[api-requests.http](api-requests.http)** - Requisições HTTP prontas
- **[API_COLLECTIONS_README.md](src/main/resources/API_COLLECTIONS_README.md)** - Collections Postman/Insomnia

---

## 🎨 Usando o IntelliJ HTTP Client

1. Abra o arquivo `api-requests.http`
2. Clique no ícone ▶️ ao lado de cada requisição
3. Veja os resultados no painel inferior

---

## ⚙️ Variáveis de Ambiente (.env)

O projeto usa variáveis de ambiente definidas no arquivo `.env`:

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

Para personalizar, edite o arquivo `.env` após criá-lo com:
```bash
cp .env.example .env
```

---

## ✅ Checklist de Verificação

### Pré-requisitos
- [ ] Java 21 instalado
- [ ] Docker e Docker Compose instalados
- [ ] Git instalado (para clonar o projeto)

### Primeira Execução
- [ ] Criar arquivo `.env`: `cp .env.example .env`
- [ ] Iniciar containers: `docker-compose up -d`
- [ ] Validar ambiente: `./validate-docker.sh`
- [ ] Executar aplicação: `./gradlew bootRun`

### Verificação da API
- [ ] Acessar http://localhost:8080/api/notes
- [ ] Criar uma nota via API
- [ ] Acessar pgAdmin em http://localhost:5050
- [ ] Ver tabela `note` no pgAdmin

### Testes
- [ ] Executar testes: `./gradlew test`
- [ ] Verificar relatório de testes em `build/reports/tests/test/index.html`

---

## 🔧 Dicas Úteis

### Desenvolvimento
- 🐳 Use `./start.sh` para iniciar tudo automaticamente
- 🔄 Use `./restart.sh` após mudanças no Docker
- 🛑 Use `./stop.sh` para parar containers (preserva dados)
- 📋 Logs SQL aparecem no console (modo dev)
- 🎨 pgAdmin é ótimo para visualizar e gerenciar o banco
- 🔍 Use `./validate-docker.sh` para verificar se está tudo OK

### Atalhos
```bash
# Compilar sem executar
./gradlew build

# Limpar e compilar
./gradlew clean build

# Apenas testes
./gradlew test

# Ver dependências
./gradlew dependencies
```

---

## 🐛 Solução de Problemas

### Arquivo .env não encontrado
```bash
cp .env.example .env
```

### Docker não está rodando
```bash
# Inicie o Docker Desktop e tente novamente
./start.sh
```

### Porta 5432 ocupada (PostgreSQL local rodando)
```bash
# Opção 1: Parar PostgreSQL local
brew services stop postgresql

# Opção 2: Alterar porta no .env
POSTGRES_PORT=5433
```

### Porta 8080 ocupada
```bash
# Alterar no .env
APP_PORT=8081

# E atualizar application.yml
server:
  port: 8081
```

### Containers não iniciam
```bash
# Ver logs
docker-compose logs -f

# Recriar containers
docker-compose down
docker-compose up -d
```

### PostgreSQL não fica saudável
```bash
# Verificar logs
docker-compose logs postgres

# Reiniciar apenas o PostgreSQL
docker-compose restart postgres
```

### Não vejo tabelas no pgAdmin
⚠️ **Use `postgres` como host, NÃO `localhost`!**

1. No pgAdmin, ao adicionar servidor
2. Connection → Host: `postgres`
3. Isso é o nome do container na rede Docker

### Build falhou
```bash
# Limpar e recompilar
./gradlew clean build --refresh-dependencies

# Verificar versão do Java
java -version  # Deve ser 21
```

### Aplicação não conecta ao banco
```bash
# 1. Verificar se containers estão rodando
docker-compose ps

# 2. Verificar logs do PostgreSQL
docker-compose logs postgres

# 3. Testar conexão
docker-compose exec postgres psql -U postgres -d notesdb -c "SELECT 1;"
```

---

## 🎉 Pronto para Começar!

### Início Rápido (3 comandos)
```bash
# 1. Criar arquivo .env
cp .env.example .env

# 2. Iniciar tudo
./start.sh

# 3. Testar API
curl http://localhost:8080/api/notes
```

### Ou Passo a Passo
```bash
# 1. Criar .env
cp .env.example .env

# 2. Iniciar containers
docker-compose up -d

# 3. Validar ambiente
./validate-docker.sh

# 4. Executar aplicação
./gradlew bootRun

# 5. Testar (em outro terminal)
curl http://localhost:8080/api/notes
```

**Sucesso! 🎊 Sua API está rodando!**

