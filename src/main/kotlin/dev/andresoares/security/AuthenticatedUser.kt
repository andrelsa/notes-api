package dev.andresoares.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Principal customizado que carrega id, name, email e roles do usuário autenticado
 * diretamente no SecurityContext, evitando queries adicionais ao banco.
 *
 * Populado pelo JwtAuthenticationFilter via uma única query (loadUserById),
 * permitindo que SecurityUtils e controllers leiam id, email, name e roles
 * sem nenhuma query adicional ao banco de dados.
 */
data class AuthenticatedUser(
    val id: Long,
    val email: String,
    val name: String,
    private val roles: Set<String>
) : UserDetails {

    private val grantedAuthorities: Collection<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority(it) }

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities
    override fun getPassword(): String = ""
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
