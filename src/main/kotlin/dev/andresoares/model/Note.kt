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

    @field:NotBlank(message = "Title is required")
    @Column(nullable = false)
    var title: String,

    @field:NotBlank(message = "Content is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    /**
     * ID do usuário proprietário desta nota.
     * Relacionamento ManyToOne: um usuário pode ter muitas notas,
     * mas cada nota pertence a exatamente um usuário.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

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
