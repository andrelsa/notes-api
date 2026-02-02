package dev.andresoares.repository

import dev.andresoares.model.Note
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByTitleContainingIgnoreCase(title: String): List<Note>
}
