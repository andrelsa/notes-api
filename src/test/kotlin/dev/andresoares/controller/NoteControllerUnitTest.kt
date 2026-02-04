package dev.andresoares.controller

import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteResponse
import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.service.NoteService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus

/**
 * Teste unitário do NoteController usando MockK.
 *
 * Este teste demonstra os benefícios de usar interfaces:
 * - Facilita criação de mocks
 * - Testa o controller isoladamente
 * - Não depende da implementação real do serviço
 * - Execução rápida (não precisa subir o Spring Context)
 */
class NoteControllerUnitTest {

    private lateinit var NoteService: NoteService
    private lateinit var noteController: NoteController

    @BeforeEach
    fun setup() {
        // Criando mock da interface INoteService
        NoteService = mockk<NoteService>()
        noteController = NoteController(NoteService)
    }

    @Test
    fun `getAllNotes deve retornar lista de notas quando nao ha filtro`() {
        // Arrange
        val expectedNotes = listOf(
            NoteResponse(1L, "Nota 1", "Conteúdo 1", "2026-01-30T10:00:00", "2026-01-30T10:00:00"),
            NoteResponse(2L, "Nota 2", "Conteúdo 2", "2026-01-30T11:00:00", "2026-01-30T11:00:00")
        )
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"))
        val page = PageImpl(expectedNotes, pageable, expectedNotes.size.toLong())

        every { NoteService.getAllNotes(any()) } returns page

        // Act
        val response = noteController.getAllNotes(null, 0, 20, "id", "asc")

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 1) { NoteService.getAllNotes(any()) }
    }

    @Test
    fun `getAllNotes deve buscar por titulo quando filtro e fornecido`() {
        // Arrange
        val title = "Reunião"
        val expectedNotes = listOf(
            NoteResponse(1L, "Reunião Sprint", "Conteúdo", "2026-01-30T10:00:00", "2026-01-30T10:00:00")
        )
        every { NoteService.searchNotesByTitle(title) } returns expectedNotes

        // Act
        val response = noteController.getAllNotes(title, 0, 20, "id", "asc")

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 1) { NoteService.searchNotesByTitle(title) }
    }

    @Test
    fun `getNoteById deve retornar nota quando id existe`() {
        // Arrange
        val noteId = 1L
        val expectedNote = NoteResponse(noteId, "Nota 1", "Conteúdo", "2026-01-30T10:00:00", "2026-01-30T10:00:00")
        every { NoteService.getNoteById(noteId) } returns expectedNote

        // Act
        val response = noteController.getNoteById(noteId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedNote, response.body)
        verify(exactly = 1) { NoteService.getNoteById(noteId) }
    }

    @Test
    fun `createNote deve retornar nota criada com status CREATED`() {
        // Arrange
        val request = NoteCreateRequest(title = "Nova Nota", content = "Novo Conteúdo")
        val expectedNote = NoteResponse(1L, "Nova Nota", "Novo Conteúdo", "2026-01-30T10:00:00", "2026-01-30T10:00:00")
        every { NoteService.createNote(request) } returns expectedNote

        // Act
        val response = noteController.createNote(request)

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedNote, response.body)
        verify(exactly = 1) { NoteService.createNote(request) }
    }

    @Test
    fun `updateNote deve retornar nota atualizada`() {
        // Arrange
        val noteId = 1L
        val request = NoteUpdateRequest(title = "Título Atualizado", content = "Conteúdo Atualizado")
        val expectedNote = NoteResponse(noteId, "Título Atualizado", "Conteúdo Atualizado", "2026-01-30T10:00:00", "2026-01-30T11:00:00")
        every { NoteService.updateNote(noteId, request) } returns expectedNote

        // Act
        val response = noteController.updateNote(noteId, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedNote, response.body)
        verify(exactly = 1) { NoteService.updateNote(noteId, request) }
    }

    @Test
    fun `deleteNote deve retornar NO_CONTENT quando nota e deletada`() {
        // Arrange
        val noteId = 1L
        every { NoteService.deleteNote(noteId) } returns Unit

        // Act
        val response = noteController.deleteNote(noteId)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify(exactly = 1) { NoteService.deleteNote(noteId) }
    }
}
