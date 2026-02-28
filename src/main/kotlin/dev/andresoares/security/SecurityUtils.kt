package dev.andresoares.security

import dev.andresoares.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

/**
 * Utilitário para acessar informações do usuário autenticado no contexto de segurança.
 *
 * Centraliza a lógica de extração do principal autenticado, evitando
 * duplicação de código nos services e controllers.
 */
@Component
class SecurityUtils(
    private val userDetailsService: CustomUserDetailsService
) {

    /**
     * Retorna o ID do usuário autenticado atualmente.
     * Lança UnauthorizedException caso não haja usuário autenticado.
     */
    fun getCurrentUserId(): Long {
        val email = getCurrentUserEmail()
        return userDetailsService.getUserIdByEmail(email)
    }

    /**
     * Retorna o email do usuário autenticado atualmente.
     */
    fun getCurrentUserEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("No authenticated user found")

        val principal = authentication.principal
        if (principal is UserDetails) {
            return principal.username
        }
        throw UnauthorizedException("Unable to extract user information from security context")
    }

    /**
     * Verifica se o usuário autenticado possui uma determinada role.
     */
    fun hasRole(role: String): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        return authentication.authorities.any { it.authority == role }
    }

    /**
     * Verifica se o usuário autenticado é ADMIN.
     */
    fun isAdmin(): Boolean = hasRole("ROLE_ADMIN")

    /**
     * Verifica se o usuário autenticado é MANAGER.
     */
    fun isManager(): Boolean = hasRole("ROLE_MANAGER")

    /**
     * Verifica se o ID do usuário autenticado corresponde ao ID fornecido.
     * Útil para validar se o usuário está acessando seus próprios recursos.
     */
    fun isCurrentUser(userId: Long): Boolean {
        return try {
            getCurrentUserId() == userId
        } catch (e: Exception) {
            false
        }
    }
}
