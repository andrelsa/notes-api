# 🤖 LLM Knowledge Base - Notes API

> **Documento de Referência Principal para Agentes LLM**
> 
> Este documento contém a base de conhecimento essencial do projeto Notes API. Utilize-o como referência primária para compreender a arquitetura, padrões e tomar decisões sobre o projeto.

---

## 📋 Índice

1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Stack Tecnológica](#stack-tecnológica)
3. [Arquitetura e Padrões](#arquitetura-e-padrões)
4. [Estrutura do Projeto](#estrutura-do-projeto)
5. [Configurações e Ambiente](#configurações-e-ambiente)
6. [Regras e Convenções](#regras-e-convenções)
7. [Workflow de Desenvolvimento](#workflow-de-desenvolvimento)
8. [Manutenção de Documentação](#manutenção-de-documentação)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral do Projeto

### Propósito
API RESTful para gerenciamento de notas (Notes Management System), desenvolvida como aplicação backend moderna seguindo as melhores práticas de desenvolvimento.

### Domínio
Sistema CRUD para gerenciamento de notas com funcionalidades de:
- Criar notas
- Listar todas as notas
- Buscar nota por ID
- Buscar notas por título (search)
- Atualizar notas
- Excluir notas
- Validação de dados
- Tratamento de exceções customizadas

### Informações Básicas
- **Nome**: Notes API
- **Versão**: 1.0-SNAPSHOT
- **Grupo**: dev.andresoares
- **Porta**: 8080
- **Base Path**: `/api/notes`

---

## 🛠️ Stack Tecnológica

### Core
- **Linguagem**: Kotlin 2.2.20
- **JVM**: Java 21
- **Build Tool**: Gradle (Kotlin DSL)
- **Framework**: Spring Boot 3.2.2

### Frameworks e Bibliotecas

#### Spring Boot Starters
```kotlin
- spring-boot-starter-web           // REST API
- spring-boot-starter-data-jpa      // Persistência
- spring-boot-starter-validation    // Validação
- spring-boot-devtools              // Desenvolvimento
- spring-boot-starter-test          // Testes
```

#### Kotlin
```kotlin
- kotlin-reflect
- kotlin-stdlib-jdk8
- jackson-module-kotlin             // Serialização JSON
```

#### Banco de Dados
```kotlin
- postgresql                        // Produção
- h2                                // Testes (opcional)
```

#### Testes
```kotlin
- kotlin-test
- mockk:1.13.8                      // Mocking para Kotlin
```

### Infraestrutura
- **Docker & Docker Compose**: Containerização
- **PostgreSQL 15**: Banco de dados
- **pgAdmin**: Interface de administração

---

## 🏗️ Arquitetura e Padrões

### Padrão Arquitetural
**Arquitetura em Camadas (Layered Architecture)** com separação clara de responsabilidades:

```
┌─────────────────────────────────────┐
│         Controller Layer            │  ← REST Endpoints
├─────────────────────────────────────┤
│          Service Layer              │  ← Lógica de Negócio
├─────────────────────────────────────┤
│        Repository Layer             │  ← Acesso a Dados
├─────────────────────────────────────┤
│          Model Layer                │  ← Entidades JPA
└─────────────────────────────────────┘
```

### Estrutura de Pacotes

```
dev.andresoares/
├── NotesApiApplication.kt              # Entry Point
├── config/                             # Configurações
│   ├── WebConfig.kt                    # CORS, Web configs
│   └── DataInitializer.kt              # Dados iniciais
├── controller/                         # REST Controllers
│   └── NoteController.kt               # Endpoints de notas
├── service/                            # Lógica de Negócio
│   ├── NoteService.kt                  # Interface
│   └── NoteServiceImpl.kt              # Implementação
├── repository/                         # Acesso a Dados
│   └── NoteRepository.kt               # Spring Data JPA
├── model/                              # Entidades
│   └── Note.kt                         # Entidade Note
├── dto/                                # Data Transfer Objects
│   ├── NoteCreateRequest.kt            # DTO para criar nota
│   ├── NoteUpdateRequest.kt            # DTO para atualizar nota
│   └── NoteResponse.kt                 # DTO de resposta
└── exception/                          # Tratamento de Exceções
    ├── BusinessException.kt            # Exceções de negócio
    ├── ValidationException.kt          # Exceções de validação
    ├── SecurityException.kt            # Exceções de segurança
    ├── InfrastructureException.kt      # Exceções de infraestrutura
    ├── dto/
    │   └── ErrorResponse.kt            # DTO de erro padronizado
    └── handler/
        └── GlobalExceptionHandler.kt   # Handler global (@RestControllerAdvice)
```

### Padrões Aplicados

#### 1. **Repository Pattern**
```kotlin
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByTitleContainingIgnoreCase(title: String): List<Note>
}
```
- Abstração do acesso a dados
- Uso de Spring Data JPA

#### 2. **Service Layer Pattern**
```kotlin
interface NoteService {
    fun getAllNotes(): List<NoteResponse>
    fun getNoteById(id: Long): NoteResponse
    // ...
}

@Service
class NoteServiceImpl : NoteService {
    // Implementação da lógica de negócio
}
```
- Separação da lógica de negócio
- Injeção de dependência

#### 3. **DTO Pattern**
```kotlin
// DTO para criar nota
data class NoteCreateRequest(
    @field:NotNull(message = "Field 'title' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'title' cannot be empty or blank")
    @field:Size(min = 1, max = 255, message = "Field 'title' must be between 1 and 255 characters")
    val title: String?,
    
    @field:NotNull(message = "Field 'content' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'content' cannot be empty or blank")
    @field:Size(min = 1, max = 5000, message = "Field 'content' must be between 1 and 5000 characters")
    val content: String?
)

// DTO para atualizar nota
data class NoteUpdateRequest(
    @field:NotBlank(message = "Field 'title' cannot be empty or blank when provided")
    @field:Size(min = 1, max = 255, message = "Field 'title' must be between 1 and 255 characters when provided")
    val title: String?,
    
    @field:NotBlank(message = "Field 'content' cannot be empty or blank when provided")
    @field:Size(min = 1, max = 5000, message = "Field 'content' must be between 1 and 5000 characters when provided")
    val content: String?
)

// DTO de resposta
data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,  // String formatada (não LocalDateTime)
    val updatedAt: String   // String formatada (não LocalDateTime)
)
```
- Separação entre modelo de domínio e contratos de API
- Validação em DTOs com mensagens customizadas
- DTOs específicos para criar e atualizar (diferentes validações)
- Response com timestamps formatados como String

#### 4. **Global Exception Handling**
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    // Tratamento centralizado de exceções
}
```
- Tratamento consistente de erros
- Respostas padronizadas

### Modelo de Dados

#### Entidade Note
```kotlin
@Entity
@Table(name = "notes")
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @NotBlank
    @Column(nullable = false)
    var title: String,
    
    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,
    
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
```

**Características**:
- ID auto-incrementado
- Timestamps automáticos (createdAt, updatedAt)
- Validação em nível de entidade
- Hook `@PreUpdate` para atualizar updatedAt

---

## 📁 Estrutura do Projeto

### Diretório Raiz
```
app-notes-api/
├── build.gradle.kts              # Configuração Gradle
├── settings.gradle.kts           # Settings Gradle
├── gradle.properties             # Propriedades Gradle
├── docker-compose.yml            # Orquestração Docker
├── .env.example                  # Template variáveis ambiente
├── .env                          # Variáveis ambiente (não commitado)
├── README.md                     # Documentação principal
├── api-requests.http             # Exemplos de requisições HTTP (IntelliJ)
│
├── docs/                         # Documentação
│   ├── KNOWLEDGE_BASE.md         # Este arquivo (base de conhecimento)
│   └── QUICKSTART.md             # Guia rápido
│
├── scripts/                      # Scripts de automação
│   ├── start.sh                  # Iniciar ambiente
│   ├── stop.sh                   # Parar ambiente
│   ├── restart.sh                # Reiniciar ambiente
│   └── validate-docker.sh        # Validar Docker
│
├── src/
│   ├── main/
│   │   ├── kotlin/               # Código-fonte Kotlin
│   │   └── resources/            # Recursos
│   │       ├── application.yml   # Config principal
│   │       ├── application-dev.yml  # Config dev
│   │       ├── postman_collection.json
│   │       └── insomnia_collection.json
│   │
│   └── test/
│       ├── kotlin/               # Testes
│       └── resources/            # Recursos de teste
│
├── gradle/                       # Wrapper Gradle
└── build/                        # Artefatos de build
```

### Scripts de Automação

#### start.sh
```bash
# Inicia PostgreSQL, pgAdmin e aplicação Spring Boot
./start.sh
```
- Verifica se `.env` existe
- Inicia containers Docker (postgres, pgadmin)
- Aguarda PostgreSQL ficar pronto (health check)
- Inicia aplicação Spring Boot

#### stop.sh
```bash
# Para containers Docker (mantém dados)
./stop.sh
```
- Para todos os containers
- Preserva volumes e dados do banco

#### restart.sh
```bash
# Reinicia todo o ambiente
./restart.sh
```
- Para e reinicia os containers Docker

#### validate-docker.sh
```bash
# Valida instalação e configuração Docker
./validate-docker.sh
```
- Verifica se Docker está instalado e rodando
- Valida Docker Compose
- Testa conectividade

### Collections de API (Postman/Insomnia)

O projeto inclui collections prontas para testar a API:

**Localização**: `src/main/resources/`

#### Arquivos:
- **postman_collection.json** - Collection completa para Postman
- **insomnia_collection.json** - Collection completa para Insomnia

⚠️ **API_COLLECTIONS_README.md** - Documentação planejada (ainda não existe)

#### Como Usar:
```bash
# Postman
1. Abra Postman
2. Import → Selecione postman_collection.json
3. Use a collection "Notes API"

# Insomnia
1. Abra Insomnia
2. Preferences → Data → Import Data
3. Selecione insomnia_collection.json
```

#### Funcionalidades Incluídas:
- ✅ Todos os 6 endpoints da API
- ✅ Exemplos prontos para usar
- ✅ Variáveis de ambiente configuradas
- ✅ Casos de teste para validação
- ✅ Casos de erro (404, 400)

### Arquivo HTTP para IntelliJ/VSCode

**Localização**: `api-requests.http` (raiz do projeto)

O projeto também inclui um arquivo HTTP com exemplos de requisições que pode ser executado diretamente no IntelliJ IDEA ou VS Code (com extensão REST Client).

```http
### Listar todas as notas
GET http://localhost:8080/api/notes

### Criar nota
POST http://localhost:8080/api/notes
Content-Type: application/json

{
  "title": "Minha Nota",
  "content": "Conteúdo da nota"
}

### Buscar por ID
GET http://localhost:8080/api/notes/1

### Atualizar nota
PUT http://localhost:8080/api/notes/1
Content-Type: application/json

{
  "title": "Título Atualizado",
  "content": "Conteúdo Atualizado"
}

### Deletar nota
DELETE http://localhost:8080/api/notes/1
```

**Vantagem**: Execução rápida sem sair da IDE.

---

## ⚙️ Configurações e Ambiente

### Variáveis de Ambiente (.env)

O projeto utiliza arquivo `.env` para configuração. Para criar:

```bash
cp .env.example .env
```

**Conteúdo do .env:**
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

**Importante:**
- O arquivo `.env.example` serve como template
- Sempre copie `.env.example` para `.env` no primeiro setup
- Nunca commite o arquivo `.env` (está no .gitignore)
- O `.env.example` deve ser commitado como referência

### application.yml
```yaml
spring:
  application:
    name: notes-api
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/notesdb
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: postgres
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update          # ⚠️ use 'validate' em produção
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  jackson:
    serialization:
      indent-output: true

server:
  port: 8080
```

### Docker Compose
```yaml
services:
  postgres:
    image: postgres:15-alpine
    ports: ["5432:5432"]
    healthcheck: # Health check configurado
    
  pgadmin:
    image: dpage/pgadmin4:latest
    ports: ["5050:80"]
    depends_on: [postgres]
```

### URLs do Ambiente

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| API | http://localhost:8080/api/notes | - |
| PostgreSQL | localhost:5432 | postgres/postgres |
| pgAdmin | http://localhost:5050 | admin@notesapi.com/admin |

### Acessar pgAdmin

Para gerenciar o banco de dados PostgreSQL através do pgAdmin:

1. **Acessar**: `http://localhost:5050`
2. **Login**:
   - Email: `admin@notesapi.com`
   - Senha: `admin`
3. **Adicionar servidor PostgreSQL**:
   - **General → Name**: NotesDB (ou qualquer nome)
   - **Connection → Host**: `postgres` (⚠️ **importante**: use "postgres", não "localhost")
   - **Connection → Port**: `5432`
   - **Connection → Database**: `notesdb`
   - **Connection → Username**: `postgres`
   - **Connection → Password**: `postgres`

### Conectar ao PostgreSQL via CLI

```bash
# Conectar ao PostgreSQL dentro do container
docker-compose exec postgres psql -U postgres -d notesdb
```

**Comandos úteis no psql:**
```sql
-- Listar tabelas
\dt

-- Ver estrutura da tabela notes
\d notes

-- Ver dados
SELECT * FROM notes;

-- Contar registros
SELECT COUNT(*) FROM notes;

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

### Gerenciamento de Containers

```bash
# Ver logs do PostgreSQL
docker-compose logs -f postgres

# Ver logs do pgAdmin
docker-compose logs -f pgadmin

# Ver status dos containers
docker-compose ps

# Reiniciar apenas o PostgreSQL
docker-compose restart postgres

# Parar e remover volumes (⚠️ apaga os dados!)
docker-compose down -v
```

---

## 📐 Regras e Convenções

### Convenções de Código

#### 1. **Nomenclatura**
- **Classes**: PascalCase (ex: `NoteController`, `NoteService`)
- **Funções/Métodos**: camelCase (ex: `getAllNotes`, `createNote`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `MAX_TITLE_LENGTH`)
- **Pacotes**: lowercase (ex: `controller`, `service`)

#### 2. **Kotlin Idioms**
```kotlin
// ✅ Usar data classes para DTOs
data class NoteResponse(...)

// ✅ Usar expressões ao invés de statements
fun getNoteOrDefault() = noteRepository.findById(id) ?: defaultNote

// ✅ Usar named parameters para clareza
createNote(title = "Test", content = "Content")

// ✅ Usar elvis operator
val title = note.title ?: "Untitled"
```

#### 3. **Injeção de Dependência**
```kotlin
// ✅ Constructor injection (preferencial)
@RestController
class NoteController(private val noteService: NoteService)

// ❌ Evitar field injection
@Autowired
lateinit var noteService: NoteService
```

#### 4. **Validação**
```kotlin
// ✅ Validação em DTOs com mensagens customizadas
data class NoteCreateRequest(
    @field:NotNull(message = "Field 'title' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'title' cannot be empty or blank")
    @field:Size(min = 1, max = 255, message = "Field 'title' must be between 1 and 255 characters")
    val title: String?
)

// Controller com @Valid
@PostMapping
fun createNote(@Valid @RequestBody request: NoteCreateRequest): ResponseEntity<NoteResponse>
```
- Validação em múltiplos níveis (@NotNull, @NotBlank, @Size)
- Mensagens de erro customizadas e descritivas
- Validação automática pelo Spring com @Valid

### REST API Conventions

#### Endpoints
```
GET    /api/notes              # Listar todas
GET    /api/notes?title=search # Buscar por título
GET    /api/notes/{id}         # Buscar por ID
POST   /api/notes              # Criar
PUT    /api/notes/{id}         # Atualizar
DELETE /api/notes/{id}         # Deletar
```

#### Status Codes
- `200 OK`: GET, PUT bem-sucedidos
- `201 Created`: POST bem-sucedido
- `204 No Content`: DELETE bem-sucedido
- `400 Bad Request`: Validação falhou
- `404 Not Found`: Recurso não encontrado
- `500 Internal Server Error`: Erro do servidor

#### Response Format
```json
// Sucesso
{
  "id": 1,
  "title": "Note Title",
  "content": "Note content",
  "createdAt": "2026-02-02T10:00:00",
  "updatedAt": "2026-02-02T10:00:00"
}

// Erro
{
  "timestamp": "2026-02-02T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Note not found with id: 1",
  "path": "/api/notes/1"
}
```

### Tratamento de Exceções

#### Hierarquia
```
Exception
├── BusinessException           # Regras de negócio
├── ValidationException         # Validação de dados
├── SecurityException          # Segurança/Autorização
└── InfrastructureException    # Problemas de infraestrutura
```

#### Uso
```kotlin
// Lançar exceção customizada
throw BusinessException("Note not found with id: $id")

// Capturar no GlobalExceptionHandler
@ExceptionHandler(BusinessException::class)
fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse>
```

### Testes

#### Tipos
1. **Unit Tests**: Testar lógica isolada (com MockK)
   - `NoteControllerUnitTest.kt` - Testes unitários do controller
   
2. **Integration Tests**: Testar fluxo completo com banco
   - `NoteControllerIntegrationTest.kt` - Testes de integração
   - `GlobalExceptionHandlerTest.kt` - Testes do exception handler

#### Nomenclatura
```kotlin
// Unit Test
class NoteControllerUnitTest {
    @Test
    fun `should create note successfully`() { }
}

// Integration Test
@SpringBootTest
class NoteControllerIntegrationTest {
    @Test
    fun `should return all notes from database`() { }
}
```

#### Executar Testes
```bash
# Todos os testes
./gradlew test

# Teste específico
./gradlew test --tests "NoteControllerUnitTest"

# Com relatório
./gradlew test
open build/reports/tests/test/index.html
```

---

## 🔄 Workflow de Desenvolvimento

### 1. Setup Inicial
```bash
# Clonar projeto
git clone <repo-url>
cd app-notes-api

# Configurar ambiente
cp .env.example .env

# Iniciar infraestrutura
./start.sh
```

### 2. Desenvolvimento
```bash
# Executar aplicação em modo dev
./gradlew bootRun

# Rodar testes
./gradlew test

# Build do projeto
./gradlew build

# Limpar e rebuild
./gradlew clean build
```

### 3. Adicionar Nova Funcionalidade

#### Checklist
1. **Model**: Criar/atualizar entidade JPA
2. **Repository**: Adicionar queries necessárias
3. **DTO**: Criar Request/Response DTOs
4. **Service**: Implementar lógica de negócio
5. **Controller**: Adicionar endpoints REST
6. **Exception**: Criar exceções customizadas se necessário
7. **Tests**: Escrever testes unitários e integração
8. **Documentation**: Atualizar documentação

#### Exemplo: Adicionar campo "tags" em Note
```kotlin
// 1. Atualizar Model
@Entity
data class Note(
    // ...existing fields...
    @ElementCollection
    val tags: List<String> = emptyList()
)

// 2. Atualizar DTOs
data class NoteCreateRequest(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)

// 3. Atualizar Service
fun createNote(request: NoteCreateRequest): NoteResponse {
    val note = Note(
        title = request.title,
        content = request.content,
        tags = request.tags
    )
    // ...
}

// 4. Testes
@Test
fun `should create note with tags`() { }
```

### 4. Debugging
```bash
# Logs da aplicação
./gradlew bootRun --debug

# Logs Docker
docker-compose logs -f

# Verificar banco de dados
# Usar pgAdmin: http://localhost:5050
```

---

## 📚 Manutenção de Documentação

### ⚠️ REGRA CRÍTICA: Sincronização de Documentação

> **IMPORTANTE**: Sempre que houver alteração na estrutura do projeto, os seguintes arquivos DEVEM ser atualizados:

#### Documentações Principais do Projeto:

**Raiz:**
- **README.md** - Visão geral, quick start, endpoints, Docker, exemplos de uso

**Docs (docs/):**
- **KNOWLEDGE_BASE.md** - Este documento (base de conhecimento para LLMs)
- **QUICKSTART.md** - Guia rápido de início

#### Documentações Planejadas:

⚠️ **Atenção**: Os seguintes documentos são mencionados no README.md mas NÃO existem ainda no projeto. Verificar antes de referenciar:

- **API_COLLECTIONS_README.md** (src/main/resources/) - Guia das collections Postman/Insomnia

- QUICKSTART.md - Guia completo para iniciar o projeto

**Recomendação**: Ao criar novas funcionalidades ou mudanças significativas, considere criar esses documentos adicionais para melhor organização.

### Gatilhos de Atualização

Atualizar documentação quando:

#### ✅ Mudanças Estruturais
- [ ] Adicionar/remover pacotes
- [ ] Adicionar/remover classes principais
- [ ] Modificar estrutura de diretórios
- [ ] Alterar arquitetura ou padrões

#### ✅ Mudanças de Configuração
- [ ] Adicionar/modificar dependências no `build.gradle.kts`
- [ ] Alterar configurações do `application.yml`
- [ ] Modificar `docker-compose.yml`
- [ ] Adicionar/modificar variáveis de ambiente

#### ✅ Mudanças de API
- [ ] Adicionar/modificar endpoints REST
- [ ] Alterar contratos de Request/Response
- [ ] Modificar status codes ou error handling

#### ✅ Mudanças de Infraestrutura
- [ ] Atualizar versões de tecnologias
- [ ] Adicionar novos serviços Docker
- [ ] Modificar scripts de automação

### Processo de Atualização

1. **Identificar impacto**: Determinar quais seções precisam atualização
2. **Atualizar KNOWLEDGE_BASE.md**: Base de conhecimento primeiro (docs/)
3. **Atualizar README.md**: Visão geral e estrutura (raiz)
4. **Atualizar QUICKSTART.md**: Comandos e quick start se necessário (docs/)
5. **Validar**: Verificar consistência entre documentos

### Template de Commit para Docs
```
docs: update documentation for [feature/change]

- Updated KNOWLEDGE_BASE.md: [changes]
- Updated README.md: [changes]
- Updated QUICKSTART.md: [changes]

Refs: #issue-number
```

---

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. Porta 8080 já em uso
```bash
# Identificar processo
lsof -i :8080

# Matar processo
kill -9 <PID>

# Ou alterar porta em application.yml
server:
  port: 8081
```

#### 2. Docker não inicia
```bash
# Validar instalação Docker
./scripts/validate-docker.sh

# Verificar status
docker-compose ps

# Reiniciar Docker Desktop (macOS)
```

#### 3. Erro de conexão com PostgreSQL
```bash
# Verificar se container está rodando
docker-compose ps

# Verificar logs
docker-compose logs postgres

# Verificar health check
docker inspect notesdb-postgres | grep Health
```

#### 4. Erro de build Gradle
```bash
# Limpar cache
./gradlew clean

# Atualizar dependências
./gradlew build --refresh-dependencies

# Verificar versão Java
java -version  # Deve ser 21
```

#### 5. Testes falhando
```bash
# Rodar teste específico
./gradlew test --tests "NoteControllerTest"

# Ver relatório detalhado
open build/reports/tests/test/index.html
```

### Logs e Debugging

```bash
# Logs da aplicação
./gradlew bootRun

# Logs Docker
docker-compose logs -f

# Logs específicos
docker-compose logs postgres
docker-compose logs pgadmin

# Debug mode
./gradlew bootRun --debug-jvm
```

### Comandos Úteis

```bash
# Verificar aplicação rodando
curl http://localhost:8080/api/notes

# Testar POST
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","content":"Content"}'

# Verificar PostgreSQL
docker exec -it notesdb-postgres psql -U postgres -d notesdb
```

---

## 🎯 Diretrizes para LLM

### Ao Analisar o Projeto

1. **Sempre referenciar** este documento como fonte primária de verdade
2. **Verificar padrões** estabelecidos antes de sugerir mudanças
3. **Manter consistência** com a arquitetura existente
4. **Seguir convenções** de nomenclatura e código Kotlin

### Ao Fazer Mudanças

1. **Avaliar impacto** na arquitetura e documentação
2. **Seguir checklist** de desenvolvimento
3. **Atualizar documentação** obrigatoriamente
4. **Adicionar testes** para novas funcionalidades
5. **Manter padrões** de exceções e validação

### Ao Responder Perguntas

1. **Referenciar seções** específicas deste documento
2. **Citar código** existente como exemplo
3. **Sugerir padrões** estabelecidos no projeto
4. **Indicar arquivos** relevantes para consulta

### Ao Sugerir Melhorias

1. **Avaliar compatibilidade** com stack atual
2. **Considerar trade-offs** de complexidade
3. **Propor migração gradual** se necessário
4. **Documentar decisão** e justificativa

---

## 📖 Referências Adicionais

### Documentação do Projeto

**Documentação Principal:**
- [README.md](../README.md) - Visão geral completa do projeto
  - Quick start
  - Endpoints da API
  - Docker e gerenciamento do banco
  - Collections Postman/Insomnia
  - Exemplos de uso

**Guias Rápidos:**
- [QUICKSTART.md](QUICKSTART.md) - Guia rápido de início
  - Comandos de inicialização
  - URLs e credenciais
  - Exemplos de API

**Documentações Planejadas (ainda não existem no projeto):**
- API_COLLECTIONS_README.md (src/main/resources/) - Guia das collections Postman/Insomnia
- DOCKER.md - Guia completo de Docker
- DESENVOLVIMENTO.md - Guia de desenvolvimento
- ARQUITETURA.md - Diagramas e arquitetura
- ESTRUTURA.md - Estrutura do projeto
- SETUP_COMPLETO.md - Setup detalhado
- SCRIPTS_GUIDE.md - Guia dos scripts
- CONCLUSAO.md - Status e resumo
- CONCLUSAO.md - Status e resumo

### Documentação Externa
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)

### Ferramentas
- [Gradle Build Tool](https://docs.gradle.org/)
- [MockK](https://mockk.io/)
- [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/)

---

## 📝 Histórico de Mudanças

| Data | Versão | Descrição | Autor |
|------|--------|-----------|-------|
| 2026-02-02 | 1.1.0 | Atualização completa com informações do projeto real | Sistema |
| 2026-02-02 | 1.0.0 | Criação inicial do documento | Sistema |

**Principais mudanças na v1.1.0:**
- ✅ Estrutura de pacotes atualizada (exception/handler, exception/dto)
- ✅ DTOs reais com validações completas (@NotNull, @NotBlank, @Size)
- ✅ Informações sobre collections Postman/Insomnia
- ✅ Detalhes sobre arquivo api-requests.http
- ✅ Comandos PostgreSQL e pgAdmin expandidos
- ✅ Informações sobre testes (NoteControllerUnitTest, NoteControllerIntegrationTest)
- ✅ Seção sobre .env e .env.example
- ✅ Documentações existentes listadas corretamente
- ✅ Processo de atualização de docs revisado

---

## 📌 Notas Finais

Este documento é a **fonte única de verdade** para agentes LLM trabalhando neste projeto. Mantenha-o atualizado e sincronizado com o código e outras documentações.

**Lembre-se**: Documentação desatualizada é pior que nenhuma documentação!

---

*Última atualização: 2026-02-02 | Versão 1.1.0*
