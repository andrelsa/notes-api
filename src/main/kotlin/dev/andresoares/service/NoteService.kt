package dev.andresoares.service

import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteResponse
import dev.andresoares.dto.NoteUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Interface que define o contrato para operações de serviço relacionadas a Notes.
 *
 * Esta interface promove:
 * - Desacoplamento entre camadas
 * - Facilidade para testes (mocks/stubs)
 * - Inversão de dependência (SOLID)
 * - Possibilidade de múltiplas implementações
 *
 * Regras de autorização por role:
 * - ROLE_ADMIN   → acesso total: gerencia todas as notas
 * - ROLE_MANAGER → pode ver todas as notas; edita/deleta somente as próprias
 * - ROLE_USER    → cria, vê, edita e deleta apenas as próprias notas
 * - ROLE_VIEWER  → somente leitura
 */
interface NoteService {

    /**
     * Retorna todas as notas cadastradas.
     * @return Lista de todas as notas em formato DTO
     */
    fun getAllNotes(): List<NoteResponse>

    /**
     * Retorna todas as notas com paginação e ordenação.
     * @param pageable Configurações de paginação e ordenação
     * @return Página de notas em formato DTO
     */
    fun getAllNotes(pageable: Pageable): Page<NoteResponse>

    /**
     * Busca uma nota específica por ID.
     * @param id Identificador da nota
     * @return Nota encontrada em formato DTO
     * @throws ResourceNotFoundException se a nota não for encontrada
     */
    fun getNoteById(id: Long): NoteResponse

    /**
     * Busca notas por título (case-insensitive).
     * @param title Texto para busca no título
     * @return Lista de notas que contêm o título buscado
     */
    fun searchNotesByTitle(title: String): List<NoteResponse>

    /**
     * Cria uma nova nota associada ao usuário autenticado.
     * @param request Dados para criação da nota
     * @return Nota criada em formato DTO
     */
    fun createNote(request: NoteCreateRequest): NoteResponse

    /**
     * Atualiza uma nota existente. Valida se o usuário autenticado tem permissão
     * (proprietário da nota, ADMIN ou MANAGER se for a sua).
     * @param id Identificador da nota a ser atualizada
     * @param request Dados para atualização da nota
     * @return Nota atualizada em formato DTO
     * @throws ResourceNotFoundException se a nota não for encontrada
     */
    fun updateNote(id: Long, request: NoteUpdateRequest): NoteResponse

    /**
     * Deleta uma nota por ID. Valida se o usuário autenticado tem permissão
     * (proprietário da nota ou ADMIN).
     * @param id Identificador da nota a ser deletada
     * @throws ResourceNotFoundException se a nota não for encontrada
     */
    fun deleteNote(id: Long)

    /**
     * Retorna todas as notas do usuário autenticado com paginação e ordenação.
     * @param pageable Configurações de paginação e ordenação
     * @return Página de notas do usuário em formato DTO
     */
    fun getMyNotes(pageable: Pageable): Page<NoteResponse>

    /**
     * Retorna todas as notas do usuário autenticado por título (case-insensitive).
     * @param title Texto para busca no título
     * @return Lista de notas do usuário que contêm o título buscado
     */
    fun searchMyNotesByTitle(title: String): List<NoteResponse>
}
