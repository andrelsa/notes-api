package dev.andresoares.repository

import dev.andresoares.model.RefreshToken
import dev.andresoares.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByUser(user: User): List<RefreshToken>
    fun deleteByUser(user: User)

    /** Remove todos os tokens expirados OU revogados — usado pelo scheduler de limpeza. */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :now OR t.revoked = true")
    fun deleteExpiredAndRevoked(now: LocalDateTime): Int
}
