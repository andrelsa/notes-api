package dev.andresoares.security

import dev.andresoares.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }

        return AuthenticatedUser(
            id = user.id!!,
            email = user.email,
            name = user.name,
            roles = user.roles
        )
    }

    /**
     * Carrega o usuário pelo ID e retorna um AuthenticatedUser populado com
     * id, email, name e roles — usado pelo JwtAuthenticationFilter para
     * popular o SecurityContext com todos os dados necessários em uma única query.
     */
    fun loadUserById(userId: Long): AuthenticatedUser {
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found with id: $userId") }

        return AuthenticatedUser(
            id = user.id!!,
            email = user.email,
            name = user.name,
            roles = user.roles
        )
    }
}
