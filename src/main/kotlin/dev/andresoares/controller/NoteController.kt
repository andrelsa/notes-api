package dev.andresoares.controller

import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteResponse
import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.service.NoteService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Controller de notas com controle de autorização baseado em roles.
 *
 * Regras de acesso:
 * - GET /api/v1/notes          → ADMIN, MANAGER (todas as notas)
 * - GET /api/v1/notes/me       → USER, VIEWER, MANAGER, ADMIN (notas próprias)
 * - GET /api/v1/notes/{id}     → qualquer autenticado (service valida ownership)
 * - POST /api/v1/notes         → USER, MANAGER, ADMIN (cria nota)
 * - PATCH /api/v1/notes/{id}   → USER, MANAGER, ADMIN (service valida ownership)
 * - DELETE /api/v1/notes/{id}  → USER, MANAGER, ADMIN (service valida ownership)
 */
@RestController
@RequestMapping("/api/v1/notes")
class NoteController(private val noteService: NoteService) {

    /**
     * Lista todas as notas do sistema.
     * Apenas ADMIN e MANAGER podem ver todas as notas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    fun getAllNotes(
        @RequestParam(required = false) title: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<*> {
        if (title != null) {
            val notes = noteService.searchNotesByTitle(title)
            return ResponseEntity.ok(notes)
        }

        val sortDirection = if (direction.equals("desc", ignoreCase = true)) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        val notesPage: Page<NoteResponse> = noteService.getAllNotes(pageable)
        return ResponseEntity.ok(notesPage)
    }

    /**
     * Lista as notas do usuário autenticado.
     * Qualquer usuário autenticado pode acessar suas próprias notas.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getMyNotes(
        @RequestParam(required = false) title: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<*> {
        if (title != null) {
            val notes = noteService.searchMyNotesByTitle(title)
            return ResponseEntity.ok(notes)
        }

        val sortDirection = if (direction.equals("desc", ignoreCase = true)) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        val notesPage: Page<NoteResponse> = noteService.getMyNotes(pageable)
        return ResponseEntity.ok(notesPage)
    }

    /**
     * Busca uma nota por ID.
     * O service valida se o usuário tem permissão para ver esta nota.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id)
        return ResponseEntity.ok(note)
    }

    /**
     * Cria uma nova nota para o usuário autenticado.
     * VIEWER não pode criar notas.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    fun createNote(@Valid @RequestBody request: NoteCreateRequest): ResponseEntity<NoteResponse> {
        val note = noteService.createNote(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(note)
    }

    /**
     * Atualiza uma nota existente.
     * O service valida se o usuário é o proprietário (exceto ADMIN).
     * VIEWER não pode editar notas.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: NoteUpdateRequest
    ): ResponseEntity<NoteResponse> {
        val note = noteService.updateNote(id, request)
        return ResponseEntity.ok(note)
    }

    /**
     * Deleta uma nota.
     * O service valida se o usuário é o proprietário (exceto ADMIN).
     * VIEWER não pode deletar notas.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    fun deleteNote(@PathVariable id: Long): ResponseEntity<Void> {
        noteService.deleteNote(id)
        return ResponseEntity.noContent().build()
    }
}
