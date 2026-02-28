package dev.andresoares.service.impl

import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteResponse
import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.exception.AccessDeniedException
import dev.andresoares.exception.ResourceNotFoundException
import dev.andresoares.model.Note
import dev.andresoares.repository.NoteRepository
import dev.andresoares.repository.UserRepository
import dev.andresoares.security.SecurityUtils
import dev.andresoares.service.NoteService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoteServiceImpl(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val securityUtils: SecurityUtils
) : NoteService {

    override fun getAllNotes(): List<NoteResponse> {
        return noteRepository.findAll().map { it.toResponse() }
    }

    override fun getAllNotes(pageable: Pageable): Page<NoteResponse> {
        return noteRepository.findAll(pageable).map { it.toResponse() }
    }

    override fun getNoteById(id: Long): NoteResponse {
        val note = noteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Note not found with id: $id") }

        // VIEWER e USER só podem ver suas próprias notas
        // MANAGER e ADMIN podem ver qualquer nota
        if (!securityUtils.isAdmin() && !securityUtils.isManager()) {
            val currentUserId = securityUtils.getCurrentUserId()
            val noteOwnerId = note.user.id
            if (noteOwnerId != currentUserId) {
                throw AccessDeniedException("You don't have permission to view this note")
            }
        }

        return note.toResponse()
    }

    override fun searchNotesByTitle(title: String): List<NoteResponse> {
        // ADMIN e MANAGER podem buscar em todas as notas
        if (securityUtils.isAdmin() || securityUtils.isManager()) {
            return noteRepository.findByTitleContainingIgnoreCase(title).map { it.toResponse() }
        }
        // USER e VIEWER buscam apenas nas suas próprias notas
        val currentUserId = securityUtils.getCurrentUserId()
        return noteRepository.findByUser_IdAndTitleContainingIgnoreCase(currentUserId, title)
            .map { it.toResponse() }
    }

    @Transactional
    override fun createNote(request: NoteCreateRequest): NoteResponse {
        val currentUserId = securityUtils.getCurrentUserId()
        val user = userRepository.findById(currentUserId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $currentUserId") }

        val note = Note(
            title = request.title!!,
            content = request.content!!,
            user = user
        )
        val savedNote = noteRepository.save(note)
        return savedNote.toResponse()
    }

    @Transactional
    override fun updateNote(id: Long, request: NoteUpdateRequest): NoteResponse {
        val note = noteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Note not found with id: $id") }

        // ADMIN pode editar qualquer nota
        // MANAGER e USER só podem editar suas próprias notas
        if (!securityUtils.isAdmin()) {
            val currentUserId = securityUtils.getCurrentUserId()
            val noteOwnerId = note.user.id
            if (noteOwnerId != null && noteOwnerId != currentUserId) {
                throw AccessDeniedException("You don't have permission to update this note")
            }
        }

        request.title?.let { note.title = it }
        request.content?.let { note.content = it }

        val updatedNote = noteRepository.save(note)
        return updatedNote.toResponse()
    }

    @Transactional
    override fun deleteNote(id: Long) {
        val note = noteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Note not found with id: $id") }

        // ADMIN pode deletar qualquer nota
        // MANAGER e USER só podem deletar suas próprias notas
        if (!securityUtils.isAdmin()) {
            val currentUserId = securityUtils.getCurrentUserId()
            val noteOwnerId = note.user.id
            if (noteOwnerId != null && noteOwnerId != currentUserId) {
                throw AccessDeniedException("You don't have permission to delete this note")
            }
        }

        noteRepository.deleteById(id)
    }

    override fun getMyNotes(pageable: Pageable): Page<NoteResponse> {
        val currentUserId = securityUtils.getCurrentUserId()
        return noteRepository.findByUser_Id(currentUserId, pageable).map { it.toResponse() }
    }

    override fun searchMyNotesByTitle(title: String): List<NoteResponse> {
        val currentUserId = securityUtils.getCurrentUserId()
        return noteRepository.findByUser_IdAndTitleContainingIgnoreCase(currentUserId, title)
            .map { it.toResponse() }
    }

    private fun Note.toResponse() = NoteResponse(
        id = id!!,
        title = title,
        content = content,
        userId = user.id,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}