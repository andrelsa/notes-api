package dev.andresoares.config

import dev.andresoares.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val environment: Environment
) {

    private val isDevProfile: Boolean
        get() = environment.activeProfiles.contains("dev")

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                // Endpoints públicos (não requerem autenticação)
                auth.requestMatchers("/api/v1/auth/**").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll() // Registro de usuário
                auth.requestMatchers("/error").permitAll()
                if (isDevProfile) {
                    auth.requestMatchers("/h2-console/**").permitAll() // Console H2 apenas em dev
                }
                // Endpoints administrativos (requerem ROLE_ADMIN)
                auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                // Todos os outros endpoints requerem autenticação
                // Por padrão, qualquer usuário autenticado (com qualquer role) pode acessar
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        // Permitir frames para H2 console apenas em dev
        if (isDevProfile) {
            http.headers { headers -> headers.frameOptions { it.sameOrigin() } }
        }

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }
}
