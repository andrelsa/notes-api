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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notes")
class NoteController(private val noteService: NoteService) {

    @GetMapping
    fun getAllNotes(
        @RequestParam(required = false) title: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<*> {
        // Se há filtro de título, retorna lista sem paginação
        if (title != null) {
            val notes = noteService.searchNotesByTitle(title)
            return ResponseEntity.ok(notes)
        }

        // Sem filtro, retorna com paginação
        val sortDirection = if (direction.equals("desc", ignoreCase = true)) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        val notesPage: Page<NoteResponse> = noteService.getAllNotes(pageable)
        return ResponseEntity.ok(notesPage)
    }

    @GetMapping("/{id}")
    fun getNoteById(@PathVariable id: Long): ResponseEntity<NoteResponse> {
        val note = noteService.getNoteById(id)
        return ResponseEntity.ok(note)
    }

    @PostMapping
    fun createNote(@Valid @RequestBody request: NoteCreateRequest): ResponseEntity<NoteResponse> {
        val note = noteService.createNote(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(note)
    }

    @PatchMapping("/{id}")
    fun updateNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: NoteUpdateRequest
    ): ResponseEntity<NoteResponse> {
        val note = noteService.updateNote(id, request)
        return ResponseEntity.ok(note)
    }

    @DeleteMapping("/{id}")
    fun deleteNote(@PathVariable id: Long): ResponseEntity<Void> {
        noteService.deleteNote(id)
        return ResponseEntity.noContent().build()
    }
}
