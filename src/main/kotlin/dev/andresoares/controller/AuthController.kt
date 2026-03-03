package dev.andresoares.controller

import dev.andresoares.dto.*
import dev.andresoares.exception.UnauthorizedException
import dev.andresoares.security.AuthenticatedUser
import dev.andresoares.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * Endpoint de login
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para renovar o access token usando refresh token
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        val response = authService.refreshToken(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint de logout
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<MessageResponse> {
        val response = authService.logout(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Retorna o perfil do usuário autenticado.
     * GET /api/v1/auth/me
     *
     * Lê diretamente do AuthenticatedUser no SecurityContext —
     * zero queries adicionais ao banco de dados.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun me(): ResponseEntity<UserInfo> {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? AuthenticatedUser
            ?: throw UnauthorizedException("No authenticated user found")

        return ResponseEntity.ok(
            UserInfo(
                id = principal.id,
                name = principal.name,
                email = principal.email,
                roles = principal.authorities.map { it.authority }.toSet()
            )
        )
    }
}
