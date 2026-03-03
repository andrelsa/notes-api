package dev.andresoares.scheduler

import dev.andresoares.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Scheduler responsável por remover periodicamente tokens de refresh
 * expirados ou revogados da tabela refresh_tokens.
 *
 * Sem essa limpeza, tokens acumulam indefinidamente no banco, aumentando
 * o tamanho da tabela sem nenhum benefício.
 *
 * Executa todos os dias às 02:00 (horário do servidor).
 */
@Component
class TokenCleanupScheduler(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    private val logger = LoggerFactory.getLogger(TokenCleanupScheduler::class.java)

    /**
     * Remove todos os tokens expirados ou revogados.
     * Executa diariamente às 02:00.
     * Também pode ser ajustado via propriedade app.scheduler.token-cleanup.cron.
     */
    @Scheduled(cron = "\${app.scheduler.token-cleanup.cron:0 0 2 * * *}")
    @Transactional
    fun cleanExpiredTokens() {
        val now = LocalDateTime.now()
        val deleted = refreshTokenRepository.deleteExpiredAndRevoked(now)
        if (deleted > 0) {
            logger.info("Token cleanup: removed $deleted expired/revoked refresh token(s)")
        } else {
            logger.debug("Token cleanup: no expired or revoked tokens found")
        }
    }
}

