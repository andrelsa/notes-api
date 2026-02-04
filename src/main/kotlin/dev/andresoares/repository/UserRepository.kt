package dev.andresoares.dev.andresoares.repository

import dev.andresoares.dev.andresoares.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<User>
}
