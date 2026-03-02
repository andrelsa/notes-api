package dev.andresoares.controller

import dev.andresoares.dto.*
import dev.andresoares.security.SecurityUtils
import dev.andresoares.service.AuthService
import dev.andresoares.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val userService: UserService,
    private val securityUtils: SecurityUtils
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
     * Útil para o frontend verificar quem está logado e quais roles possui.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun me(): ResponseEntity<UserInfo> {
        val userId = securityUtils.getCurrentUserId()
        val userResponse = userService.getUserById(userId)
        val userInfo = UserInfo(
            id = userResponse.id,
            name = userResponse.name,
            email = userResponse.email,
            roles = userResponse.roles
        )
        return ResponseEntity.ok(userInfo)
    }
}
