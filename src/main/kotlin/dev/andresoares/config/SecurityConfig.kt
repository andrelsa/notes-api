package dev.andresoares.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import java.time.Instant

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoints públicos (não requerem autenticação)
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll() // Registro de usuário
                    .requestMatchers("/h2-console/**").permitAll() // Console H2 para dev
                    .requestMatchers("/error").permitAll()

                    // Endpoints administrativos (requerem ROLE_ADMIN)
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                    // Todos os outros endpoints requerem autenticação
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                // Retorna JSON 401 quando não autenticado
                exceptions.authenticationEntryPoint { request, response, authException ->
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    val body = mapOf(
                        "timestamp" to Instant.now().toString(),
                        "status" to 401,
                        "error" to "Unauthorized",
                        "message" to (authException.message ?: "Authentication is required"),
                        "path" to request.requestURI
                    )
                    response.writer.write(objectMapper.writeValueAsString(body))
                    response.writer.flush()
                }
                // Retorna JSON 403 quando autenticado mas sem permissão (filter-level)
                exceptions.accessDeniedHandler { request, response, _ ->
                    response.status = HttpStatus.FORBIDDEN.value()
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    val body = mapOf(
                        "timestamp" to Instant.now().toString(),
                        "status" to 403,
                        "error" to "Forbidden",
                        "message" to "You don't have permission to access this resource",
                        "path" to request.requestURI
                    )
                    response.writer.write(objectMapper.writeValueAsString(body))
                    response.writer.flush()
                }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        // Permitir frames para H2 console
        http.headers { headers -> headers.frameOptions { it.sameOrigin() } }

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

