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
     * Usuário proprietário desta nota.
     * Relacionamento ManyToOne: um usuário pode ter muitas notas.
     *
     * Notas criadas via API sempre possuem um proprietário atribuído.
     * O valor null indica notas legadas ou criadas fora do fluxo padrão.
     * A lógica de autorização nega acesso a notas sem proprietário para
     * usuários não-administradores.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    var user: User? = null,

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
