package dev.andresoares.security

import dev.andresoares.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

/**
 * Utilitário para acessar informações do usuário autenticado no contexto de segurança.
 *
 * Em produção, lê diretamente do AuthenticatedUser armazenado no SecurityContext
 * pelo JwtAuthenticationFilter — sem query adicional ao banco.
 *
 * Em testes com @WithMockUser, o principal é um UserDetails genérico; nesse caso
 * o id é resolvido via CustomUserDetailsService (apenas uma query, somente em testes).
 */
@Component
class SecurityUtils(
    private val userDetailsService: CustomUserDetailsService
) {

    private fun getPrincipal(): Any =
        SecurityContextHolder.getContext().authentication?.principal
            ?: throw UnauthorizedException("No authenticated user found")

    /** Retorna o ID do usuário autenticado. Zero queries extras em produção. */
    fun getCurrentUserId(): Long {
        return when (val principal = getPrincipal()) {
            is AuthenticatedUser -> principal.id
            is UserDetails       -> userDetailsService.loadUserByUsername(principal.username).let {
                (it as AuthenticatedUser).id
            }
            else -> throw UnauthorizedException("Unable to extract user id from security context")
        }
    }

    /** Retorna o email do usuário autenticado. */
    fun getCurrentUserEmail(): String {
        return when (val principal = getPrincipal()) {
            is AuthenticatedUser -> principal.email
            is UserDetails       -> principal.username
            else -> throw UnauthorizedException("Unable to extract email from security context")
        }
    }

    /** Verifica se o usuário autenticado possui a role informada. */
    fun hasRole(role: String): Boolean =
        SecurityContextHolder.getContext().authentication
            ?.authorities?.any { it.authority == role } ?: false

    /** Verifica se o usuário autenticado é ADMIN. */
    fun isAdmin(): Boolean = hasRole("ROLE_ADMIN")

    /** Verifica se o usuário autenticado é MANAGER. */
    fun isManager(): Boolean = hasRole("ROLE_MANAGER")

    /** Verifica se o ID do usuário autenticado corresponde ao ID fornecido. */
    fun isCurrentUser(userId: Long): Boolean =
        try { getCurrentUserId() == userId } catch (_: Exception) { false }
}


