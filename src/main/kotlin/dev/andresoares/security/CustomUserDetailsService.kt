package dev.andresoares.security

import dev.andresoares.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
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

        // Carregar roles dinamicamente do banco de dados
        val authorities = user.roles.map { SimpleGrantedAuthority(it) }

        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(authorities) // Roles dinâmicas do banco
            .build()
    }

    fun loadUserById(userId: Long): UserDetails {
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found with id: $userId") }

        // Carregar roles dinamicamente do banco de dados
        val authorities = user.roles.map { SimpleGrantedAuthority(it) }

        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(authorities) // Roles dinâmicas do banco
            .build()
    }

    /**
     * Retorna o ID do usuário a partir do email.
     * Usado pelo SecurityUtils para obter o ID do usuário autenticado.
     */
    fun getUserIdByEmail(email: String): Long {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }
        return user.id ?: throw UsernameNotFoundException("User id is null for email: $email")
    }
}
