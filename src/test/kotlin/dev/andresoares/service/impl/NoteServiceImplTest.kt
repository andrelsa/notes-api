package dev.andresoares.service.impl

import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.exception.AccessDeniedException
import dev.andresoares.exception.ResourceNotFoundException
import dev.andresoares.model.Note
import dev.andresoares.model.User
import dev.andresoares.repository.NoteRepository
import dev.andresoares.repository.UserRepository
import dev.andresoares.security.SecurityUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional

/**
 * Testes unitários do NoteServiceImpl focados na lógica de autorização.
 */
class NoteServiceImplTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var userRepository: UserRepository
    private lateinit var securityUtils: SecurityUtils
    private lateinit var noteService: NoteServiceImpl

    private val userId = 1L
    private val otherUserId = 2L

    private val user = User(
        id = userId,
        name = "Test User",
        email = "test@example.com",
        password = "hashed",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setup() {
        noteRepository = mockk()
        userRepository = mockk()
        securityUtils = mockk()
        noteService = NoteServiceImpl(noteRepository, userRepository, securityUtils)
    }

    // --- deleteNote authorization tests ---

    @Test
    fun `deleteNote should deny non-admin when note has no owner`() {
        val noteWithoutOwner = Note(
            id = 10L,
            title = "Orphan Note",
            content = "No owner",
            user = null
        )
        every { noteRepository.findById(10L) } returns Optional.of(noteWithoutOwner)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns userId

        assertThrows(AccessDeniedException::class.java) {
            noteService.deleteNote(10L)
        }

        verify(exactly = 0) { noteRepository.deleteById(any()) }
    }

    @Test
    fun `deleteNote should allow admin when note has no owner`() {
        val noteWithoutOwner = Note(
            id = 10L,
            title = "Orphan Note",
            content = "No owner",
            user = null
        )
        every { noteRepository.findById(10L) } returns Optional.of(noteWithoutOwner)
        every { securityUtils.isAdmin() } returns true
        every { noteRepository.deleteById(10L) } returns Unit

        noteService.deleteNote(10L)

        verify(exactly = 1) { noteRepository.deleteById(10L) }
    }

    @Test
    fun `deleteNote should allow owner to delete their own note`() {
        val ownedNote = Note(id = 10L, title = "My Note", content = "Content", user = user)
        every { noteRepository.findById(10L) } returns Optional.of(ownedNote)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns userId
        every { noteRepository.deleteById(10L) } returns Unit

        noteService.deleteNote(10L)

        verify(exactly = 1) { noteRepository.deleteById(10L) }
    }

    @Test
    fun `deleteNote should deny non-owner trying to delete another user note`() {
        val ownedNote = Note(id = 10L, title = "Other User Note", content = "Content", user = user)
        every { noteRepository.findById(10L) } returns Optional.of(ownedNote)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns otherUserId

        assertThrows(AccessDeniedException::class.java) {
            noteService.deleteNote(10L)
        }

        verify(exactly = 0) { noteRepository.deleteById(any()) }
    }

    // --- updateNote authorization tests ---

    @Test
    fun `updateNote should deny non-admin when note has no owner`() {
        val noteWithoutOwner = Note(
            id = 10L,
            title = "Orphan Note",
            content = "No owner",
            user = null
        )
        every { noteRepository.findById(10L) } returns Optional.of(noteWithoutOwner)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns userId

        assertThrows(AccessDeniedException::class.java) {
            noteService.updateNote(10L, NoteUpdateRequest(title = "New Title", content = null))
        }

        verify(exactly = 0) { noteRepository.save(any()) }
    }

    @Test
    fun `updateNote should allow admin when note has no owner`() {
        val noteWithoutOwner = Note(
            id = 10L,
            title = "Orphan Note",
            content = "No owner",
            user = null
        )
        every { noteRepository.findById(10L) } returns Optional.of(noteWithoutOwner)
        every { securityUtils.isAdmin() } returns true
        every { noteRepository.save(noteWithoutOwner) } returns noteWithoutOwner

        noteService.updateNote(10L, NoteUpdateRequest(title = "Updated Title", content = null))

        verify(exactly = 1) { noteRepository.save(noteWithoutOwner) }
    }

    @Test
    fun `updateNote should allow owner to update their own note`() {
        val ownedNote = Note(id = 10L, title = "My Note", content = "Content", user = user)
        every { noteRepository.findById(10L) } returns Optional.of(ownedNote)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns userId
        every { noteRepository.save(ownedNote) } returns ownedNote

        noteService.updateNote(10L, NoteUpdateRequest(title = "Updated", content = null))

        verify(exactly = 1) { noteRepository.save(ownedNote) }
    }

    @Test
    fun `updateNote should deny non-owner trying to update another user note`() {
        val ownedNote = Note(id = 10L, title = "Other User Note", content = "Content", user = user)
        every { noteRepository.findById(10L) } returns Optional.of(ownedNote)
        every { securityUtils.isAdmin() } returns false
        every { securityUtils.getCurrentUserId() } returns otherUserId

        assertThrows(AccessDeniedException::class.java) {
            noteService.updateNote(10L, NoteUpdateRequest(title = "New Title", content = null))
        }

        verify(exactly = 0) { noteRepository.save(any()) }
    }

    @Test
    fun `deleteNote should throw ResourceNotFoundException when note does not exist`() {
        every { noteRepository.findById(999L) } returns Optional.empty()

        assertThrows(ResourceNotFoundException::class.java) {
            noteService.deleteNote(999L)
        }
    }
}
