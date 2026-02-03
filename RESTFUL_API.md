# API RESTful - Conceitos e Análise do Projeto

## 📖 O que é uma API RESTful?

REST (Representational State Transfer) é um estilo arquitetural para desenvolvimento de serviços web, proposto por Roy Fielding em 2000. Uma API RESTful é uma API que segue os princípios e restrições do REST.

### Princípios Fundamentais do REST

#### 1. **Cliente-Servidor**
- Separação clara entre o cliente (interface do usuário) e o servidor (armazenamento de dados)
- Permite que cada um evolua independentemente

#### 2. **Stateless (Sem Estado)**
- Cada requisição do cliente para o servidor deve conter todas as informações necessárias
- O servidor não armazena o contexto da sessão do cliente entre requisições
- Melhora a escalabilidade e confiabilidade

#### 3. **Cacheable (Cacheável)**
- As respostas devem definir explicitamente se podem ser cacheadas ou não
- Melhora a performance e escalabilidade

#### 4. **Interface Uniforme**
Composta por quatro restrições:

##### a) Identificação de Recursos
- Recursos são identificados por URIs (Uniform Resource Identifiers)
- Exemplo: `/api/notes/1` identifica a nota com ID 1

##### b) Manipulação através de Representações
- Os recursos são manipulados através de representações (JSON, XML, etc.)
- O cliente recebe uma representação que contém informação suficiente para modificar ou deletar o recurso

##### c) Mensagens Auto-descritivas
- Cada mensagem inclui informações suficientes para descrever como processá-la
- Uso apropriado de métodos HTTP e códigos de status

##### d) HATEOAS (Hypermedia as the Engine of Application State)
- As respostas incluem links para outras ações possíveis
- Permite a navegação dinâmica pela API

#### 5. **Sistema em Camadas**
- A arquitetura pode ser composta por camadas hierárquicas
- Cada componente não pode "ver" além da camada imediata com a qual está interagindo

#### 6. **Código sob Demanda (Opcional)**
- Servidores podem estender a funcionalidade do cliente enviando código executável

### Métodos HTTP e Suas Finalidades

| Método | Finalidade | Idempotente | Seguro |
|--------|-----------|-------------|---------|
| **GET** | Recuperar recursos | ✅ Sim | ✅ Sim |
| **POST** | Criar novos recursos | ❌ Não | ❌ Não |
| **PUT** | Atualizar recursos (substituição completa) | ✅ Sim | ❌ Não |
| **PATCH** | Atualizar recursos parcialmente | ❌ Não | ❌ Não |
| **DELETE** | Remover recursos | ✅ Sim | ❌ Não |
| **HEAD** | Igual a GET, mas sem corpo da resposta | ✅ Sim | ✅ Sim |
| **OPTIONS** | Descrever opções de comunicação | ✅ Sim | ✅ Sim |

- **Idempotente**: Múltiplas requisições idênticas têm o mesmo efeito que uma única
- **Seguro**: Não modifica o estado do servidor

### Códigos de Status HTTP Importantes

#### 2xx - Sucesso
- **200 OK**: Requisição bem-sucedida
- **201 Created**: Recurso criado com sucesso
- **204 No Content**: Requisição bem-sucedida sem conteúdo para retornar

#### 3xx - Redirecionamento
- **301 Moved Permanently**: Recurso movido permanentemente
- **304 Not Modified**: Recurso não foi modificado (cache)

#### 4xx - Erros do Cliente
- **400 Bad Request**: Requisição malformada
- **401 Unauthorized**: Autenticação necessária
- **403 Forbidden**: Sem permissão para acessar
- **404 Not Found**: Recurso não encontrado
- **405 Method Not Allowed**: Método HTTP não suportado
- **409 Conflict**: Conflito com o estado atual do recurso
- **422 Unprocessable Entity**: Validação de negócio falhou

#### 5xx - Erros do Servidor
- **500 Internal Server Error**: Erro genérico do servidor
- **503 Service Unavailable**: Serviço temporariamente indisponível

### Boas Práticas RESTful

#### 1. **Nomenclatura de URIs**
- ✅ Use substantivos no plural: `/api/notes`
- ✅ Use hierarquia lógica: `/api/notes/1/comments`
- ✅ Use kebab-case: `/api/user-profiles`
- ❌ Evite verbos: `/api/getNotes`, `/api/createNote`
- ❌ Evite extensões de arquivo: `/api/notes.json`

#### 2. **Versionamento**
- `/api/v1/notes`
- `/api/v2/notes`

#### 3. **Filtros e Paginação**
- Filtros: `/api/notes?title=Spring&status=published`
- Paginação: `/api/notes?page=1&size=20`
- Ordenação: `/api/notes?sort=createdAt,desc`

#### 4. **Relacionamentos**
- `/api/notes/1/comments` - Comentários da nota 1
- `/api/users/5/notes` - Notas do usuário 5

#### 5. **Consistência**
- Manter padrões consistentes em toda a API
- Usar os mesmos nomes de campos
- Seguir o mesmo formato de resposta

---

## 🔍 Análise: O Projeto notes-api é RESTful?

### ✅ Aspectos Aderentes ao REST

#### 1. ✅ **Arquitetura Cliente-Servidor**
O projeto segue claramente a arquitetura cliente-servidor:
- **Servidor**: Spring Boot API que gerencia os dados
- **Cliente**: Qualquer aplicação que consuma a API (pode ser front-end, mobile, etc.)
- Separação completa entre camadas

#### 2. ✅ **Stateless (Sem Estado)**
A API é completamente stateless:
- Cada requisição é independente
- Não há armazenamento de sessão no servidor
- Todas as informações necessárias estão na requisição

```kotlin
@RestController
@RequestMapping("/api/notes")
class NoteController(private val noteService: NoteService) {
    // Sem gerenciamento de sessão ou estado entre requisições
}
```

#### 3. ✅ **Interface Uniforme - Identificação de Recursos**
Recursos são claramente identificados por URIs:
- `/api/notes` - Coleção de notas
- `/api/notes/{id}` - Nota específica
- URIs usam substantivos no plural ✅

#### 4. ✅ **Uso Correto dos Métodos HTTP**

| Operação | Método | Endpoint | Status Code |
|----------|--------|----------|-------------|
| Listar todas as notas | GET | `/api/notes` | 200 OK |
| Buscar nota por ID | GET | `/api/notes/{id}` | 200 OK |
| Buscar por título | GET | `/api/notes?title=x` | 200 OK |
| Criar nova nota | POST | `/api/notes` | 201 Created |
| Atualizar nota | PUT | `/api/notes/{id}` | 200 OK |
| Excluir nota | DELETE | `/api/notes/{id}` | 204 No Content |

```kotlin
@GetMapping
fun getAllNotes(@RequestParam(required = false) title: String?): ResponseEntity<List<NoteResponse>>

@GetMapping("/{id}")
fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse>

@PostMapping
fun createNote(@Valid @RequestBody request: NoteCreateRequest): ResponseEntity<NoteResponse>

@PutMapping("/{id}")
fun updateNote(@PathVariable id: Long, @Valid @RequestBody request: NoteUpdateRequest): ResponseEntity<NoteResponse>

@DeleteMapping("/{id}")
fun deleteNote(@PathVariable id: Long): ResponseEntity<Void>
```

**Análise detalhada:**
- ✅ GET para leitura
- ✅ POST para criação (retorna 201 Created)
- ✅ PUT para atualização completa
- ✅ DELETE para remoção (retorna 204 No Content)

#### 5. ✅ **Códigos de Status HTTP Apropriados**

O projeto utiliza códigos de status de forma apropriada:

```kotlin
// 200 OK para operações bem-sucedidas
return ResponseEntity.ok(note)

// 201 Created para criação de recursos
return ResponseEntity.status(HttpStatus.CREATED).body(note)

// 204 No Content para deleção bem-sucedida
return ResponseEntity.noContent().build()

// 404 Not Found para recursos não encontrados
throw ResourceNotFoundException("Note not found with id: $id")

// 400 Bad Request para validação
@ExceptionHandler(MethodArgumentNotValidException::class)

// 422 Unprocessable Entity para regras de negócio
@ExceptionHandler(BusinessRuleViolationException::class)
```

#### 6. ✅ **Representações em JSON**
A API usa JSON como formato de representação:
```kotlin
@RestController // Automaticamente serializa/deserializa JSON
data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)
```

#### 7. ✅ **Validação de Dados**
Validação robusta usando Bean Validation:
```kotlin
data class NoteCreateRequest(
    @field:NotNull(message = "Field 'title' is required")
    @field:NotBlank(message = "Field 'title' cannot be empty")
    @field:Size(min = 1, max = 255)
    val title: String?,
    
    @field:NotNull(message = "Field 'content' is required")
    @field:NotBlank(message = "Field 'content' cannot be empty")
    @field:Size(min = 1, max = 5000)
    val content: String?
)
```

#### 8. ✅ **Tratamento de Exceções Padronizado**
GlobalExceptionHandler fornece respostas de erro consistentes:
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    // Tratamento centralizado e padronizado de exceções
    // Retorna ErrorResponse estruturado em JSON
}
```

Exemplo de resposta de erro:
```json
{
  "timestamp": "2024-01-20T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Note not found with id: 999",
  "path": "/api/notes/999",
  "traceId": "abc-123-def-456"
}
```

#### 9. ✅ **Separação em Camadas**
Arquitetura em camadas bem definida:
```
Controller (API) → Service (Lógica de Negócio) → Repository (Acesso a Dados) → Database
```

```kotlin
// Camada de Apresentação
@RestController
class NoteController

// Camada de Negócio
@Service
class NoteServiceImpl

// Camada de Dados
interface NoteRepository : JpaRepository<Note, Long>
```

#### 10. ✅ **Content Negotiation**
Suporte a diferentes tipos de conteúdo:
```kotlin
// Content-Type: application/json
// Accept: application/json
```

#### 11. ✅ **Filtros e Parâmetros de Query**
Implementação de busca por parâmetros:
```kotlin
@GetMapping
fun getAllNotes(@RequestParam(required = false) title: String?): ResponseEntity<List<NoteResponse>> {
    val notes = if (title != null) {
        noteService.searchNotesByTitle(title)
    } else {
        noteService.getAllNotes()
    }
    return ResponseEntity.ok(notes)
}
```

Exemplo: `GET /api/notes?title=Spring`

#### 12. ✅ **CORS Configurado**
Configuração adequada para permitir acesso cross-origin:
```kotlin
@Configuration
class WebConfig {
    // Configuração de CORS
}
```

### ⚠️ Aspectos que Poderiam Melhorar

#### 1. ⚠️ **HATEOAS**
A API não implementa HATEOAS (Hypermedia as the Engine of Application State).

**Estado atual:**
```json
{
  "id": 1,
  "title": "Minha Nota",
  "content": "Conteúdo"
}
```

**Com HATEOAS:**
```json
{
  "id": 1,
  "title": "Minha Nota",
  "content": "Conteúdo",
  "_links": {
    "self": { "href": "/api/notes/1" },
    "update": { "href": "/api/notes/1" },
    "delete": { "href": "/api/notes/1" },
    "all": { "href": "/api/notes" }
  }
}
```

**Nota**: HATEOAS é considerado opcional e muitas APIs RESTful modernas não o implementam por questões de simplicidade.

#### 2. ⚠️ **Versionamento da API**
Não há versionamento explícito na API.

**Recomendação:**
```kotlin
@RequestMapping("/api/v1/notes")  // Em vez de /api/notes
```

#### 3. ⚠️ **Paginação**
A listagem de todas as notas não implementa paginação.

**Estado atual:**
```kotlin
fun getAllNotes(): List<NoteResponse>  // Retorna todas as notas
```

**Recomendação:**
```kotlin
fun getAllNotes(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
): Page<NoteResponse>
```

Exemplo: `GET /api/notes?page=0&size=20`

#### 4. ⚠️ **Cache Headers**
A API não define headers de cache explicitamente.

**Recomendação:**
```kotlin
return ResponseEntity.ok()
    .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
    .body(notes)
```

#### 5. ⚠️ **ETags**
Não há suporte a ETags para otimização de cache.

**Recomendação:**
```kotlin
return ResponseEntity.ok()
    .eTag(calculateETag(note))
    .body(note)
```

#### 6. ⚠️ **PATCH vs PUT**
A API usa PUT para atualização parcial, mas o ideal seria usar PATCH.

**Estado atual:**
```kotlin
@PutMapping("/{id}")
fun updateNote(...)  // Permite atualização parcial
```

**Ideal:**
```kotlin
@PutMapping("/{id}")
fun replaceNote(...)  // Substituição completa

@PatchMapping("/{id}")
fun updateNote(...)   // Atualização parcial
```

#### 7. ⚠️ **Documentação OpenAPI/Swagger**
Não há documentação interativa da API.

**Recomendação**: Adicionar SpringDoc OpenAPI para gerar documentação automática.

### 📊 Scorecard de Aderência REST

| Princípio/Prática | Status | Implementação |
|-------------------|--------|---------------|
| Cliente-Servidor | ✅ Completo | Arquitetura bem separada |
| Stateless | ✅ Completo | Sem estado entre requisições |
| Cacheable | ⚠️ Parcial | Faltam headers de cache |
| Interface Uniforme | ✅ Bom | URIs bem definidas |
| Identificação de Recursos | ✅ Completo | URIs claras e consistentes |
| Manipulação via Representações | ✅ Completo | JSON bem estruturado |
| Mensagens Auto-descritivas | ✅ Completo | Métodos HTTP e status codes corretos |
| HATEOAS | ❌ Não implementado | Sem hypermedia links |
| Sistema em Camadas | ✅ Completo | Controller→Service→Repository |
| Métodos HTTP | ✅ Completo | GET, POST, PUT, DELETE corretos |
| Códigos de Status | ✅ Completo | 200, 201, 204, 400, 404, etc. |
| Validação | ✅ Completo | Bean Validation implementado |
| Tratamento de Erros | ✅ Completo | GlobalExceptionHandler robusto |
| Content Type | ✅ Completo | JSON como padrão |
| Versionamento | ❌ Não implementado | Sem versão na URI |
| Paginação | ❌ Não implementado | Lista todos os resultados |
| Filtros | ✅ Parcial | Busca por título implementada |
| Ordenação | ❌ Não implementado | Sem parâmetro de ordenação |
| CORS | ✅ Completo | Configurado adequadamente |

**Pontuação Geral: 15/19 (79%) - BOM** ✅

---

## 🎯 Conclusão

### O projeto notes-api é RESTful? **SIM!** ✅

O projeto **notes-api** é considerado uma **API RESTful** porque:

1. **Segue os princípios fundamentais do REST:**
   - ✅ Arquitetura Cliente-Servidor
   - ✅ Stateless (sem estado)
   - ✅ Interface uniforme
   - ✅ Sistema em camadas

2. **Implementa corretamente:**
   - ✅ Métodos HTTP (GET, POST, PUT, DELETE)
   - ✅ Códigos de status HTTP apropriados
   - ✅ URIs bem estruturadas com substantivos
   - ✅ Representações JSON
   - ✅ Validação de dados
   - ✅ Tratamento de exceções padronizado

3. **Nível de Maturidade Richardson: Nível 2** 🎯
   - **Nível 0**: Uma única URI, um único método (RPC)
   - **Nível 1**: Múltiplos recursos com URIs diferentes
   - **Nível 2**: Uso correto de métodos HTTP e status codes ← **AQUI**
   - **Nível 3**: HATEOAS (hypermedia controls)

### Classificação

**API RESTful de Nível 2 (Richardson Maturity Model)**

Isso significa que a API:
- ✅ Usa múltiplos recursos com URIs bem definidas
- ✅ Utiliza métodos HTTP corretamente
- ✅ Retorna códigos de status HTTP apropriados
- ⚠️ Não implementa HATEOAS (Nível 3)

### Pontos Fortes

1. **Excelente estrutura de código** com separação clara de responsabilidades
2. **Validação robusta** com mensagens de erro claras
3. **Tratamento de exceções exemplar** com GlobalExceptionHandler
4. **Códigos de status HTTP corretos** em todas as operações
5. **URIs bem projetadas** seguindo convenções REST
6. **DTOs separados** para request e response
7. **Documentação de exemplos** (postman_collection.json, insomnia_collection.json)

### Recomendações para Nível 3 (HATEOAS)

Se desejar evoluir para uma API RESTful de Nível 3, considere:

1. **Adicionar Spring HATEOAS**
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
}
```

2. **Implementar links em respostas**
```kotlin
data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
) : RepresentationModel<NoteResponse>()

// No controller:
fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
    val note = noteService.getNoteById(id)
    note.add(linkTo(methodOn(NoteController::class.java).getNoteById(id)).withSelfRel())
    note.add(linkTo(methodOn(NoteController::class.java).getAllNotes(null)).withRel("all"))
    return ResponseEntity.ok(note)
}
```

3. **Adicionar paginação**
```kotlin
fun getAllNotes(pageable: Pageable): Page<NoteResponse>
```

4. **Implementar versionamento**
```kotlin
@RequestMapping("/api/v1/notes")
```

5. **Adicionar documentação OpenAPI**
```kotlin
dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
```

### Referências

- [Richardson Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html)
- [RESTful API Design Best Practices](https://restfulapi.net/)
- [RFC 7231 - HTTP/1.1 Semantics and Content](https://tools.ietf.org/html/rfc7231)
- [Spring HATEOAS Documentation](https://spring.io/projects/spring-hateoas)

---

**Documento criado em**: 2026-02-03  
**Projeto**: notes-api  
**Versão**: 1.0
