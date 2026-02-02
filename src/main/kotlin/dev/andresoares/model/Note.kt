package dev.andresoares.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "notes")
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    var title: String,

    @NotBlank(message = "Content is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
