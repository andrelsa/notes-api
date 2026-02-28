package dev.andresoares.repository

import dev.andresoares.model.Note
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByTitleContainingIgnoreCase(title: String): List<Note>

    // Queries para notas do usuário (ownership)
    fun findByUserId(userId: Long, pageable: Pageable): Page<Note>
    fun findByUserIdAndTitleContainingIgnoreCase(userId: Long, title: String): List<Note>
}
