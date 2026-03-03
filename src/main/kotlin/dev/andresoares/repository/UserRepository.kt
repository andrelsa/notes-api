package dev.andresoares.repository

import dev.andresoares.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<User>
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}
