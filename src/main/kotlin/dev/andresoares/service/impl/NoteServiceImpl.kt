package dev.andresoares.dev.andresoares.service.impl

import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteResponse
import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.exception.ResourceNotFoundException
import dev.andresoares.model.Note
import dev.andresoares.repository.NoteRepository
import dev.andresoares.service.NoteService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteServiceImpl(private val noteRepository: NoteRepository) : NoteService {

    override fun getAllNotes(): List<NoteResponse> {
        return noteRepository.findAll().map { it.toResponse() }
    }

    override fun getNoteById(id: Long): NoteResponse {
        val note = noteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Note not found with id: $id") }
        return note.toResponse()
    }

    override fun searchNotesByTitle(title: String): List<NoteResponse> {
        return noteRepository.findByTitleContainingIgnoreCase(title).map { it.toResponse() }
    }

    @Transactional
    override fun createNote(request: NoteCreateRequest): NoteResponse {
        // A validação @NotNull garante que title e content não serão null aqui
        val note = Note(
            title = request.title!!,
            content = request.content!!
        )
        val savedNote = noteRepository.save(note)
        return savedNote.toResponse()
    }

    @Transactional
    override fun updateNote(id: Long, request: NoteUpdateRequest): NoteResponse {
        val note = noteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Note not found with id: $id") }

        request.title?.let { note.title = it }
        request.content?.let { note.content = it }

        val updatedNote = noteRepository.save(note)
        return updatedNote.toResponse()
    }

    @Transactional
    override fun deleteNote(id: Long) {
        if (!noteRepository.existsById(id)) {
            throw ResourceNotFoundException("Note not found with id: $id")
        }
        noteRepository.deleteById(id)
    }

    private fun Note.toResponse() = NoteResponse(
        id = id!!,
        title = title,
        content = content,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}