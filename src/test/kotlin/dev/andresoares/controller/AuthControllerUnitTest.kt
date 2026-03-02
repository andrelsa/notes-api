package dev.andresoares.controller

import dev.andresoares.dto.*
import dev.andresoares.security.SecurityUtils
import dev.andresoares.service.AuthService
import dev.andresoares.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Teste unitário do AuthController usando MockK.
 *
 * Este teste demonstra os benefícios de usar interfaces:
 * - Facilita criação de mocks
 * - Testa o controller isoladamente
 * - Não depende da implementação real do serviço
 * - Execução rápida (não precisa subir o Spring Context)
 */
class AuthControllerUnitTest {

    private lateinit var authService: AuthService
    private lateinit var userService: UserService
    private lateinit var securityUtils: SecurityUtils
    private lateinit var authController: AuthController

    @BeforeEach
    fun setup() {
        // Criando mock da interface AuthService
        authService = mockk<AuthService>()
        userService = mockk<UserService>()
        securityUtils = mockk<SecurityUtils>()
        authController = AuthController(authService, userService, securityUtils)
    }

    @Test
    fun `login deve retornar token JWT quando credenciais sao validas`() {
        // Arrange
        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        val expectedResponse = LoginResponse(
            accessToken = "eyJhbGciOiJIUzUxMiJ9...",
            refreshToken = "refresh_token_mock",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = UserInfo(
                id = 1L,
                name = "João Silva",
                email = "joao.silva@example.com",
                roles = setOf("ROLE_USER")
            )
        )

        every { authService.login(loginRequest) } returns expectedResponse

        // Act
        val response = authController.login(loginRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
        assertEquals("Bearer", response.body?.tokenType)
        assertEquals(3600L, response.body?.expiresIn)
        assertEquals("joao.silva@example.com", response.body?.user?.email)
        assertEquals(setOf("ROLE_USER"), response.body?.user?.roles)
        verify(exactly = 1) { authService.login(loginRequest) }
    }

    @Test
    fun `login deve retornar usuario com multiplas roles quando usuario possui varias roles`() {
        // Arrange
        val loginRequest = LoginRequest(
            email = "admin@example.com",
            password = "admin123456"
        )

        val expectedResponse = LoginResponse(
            accessToken = "eyJhbGciOiJIUzUxMiJ9...",
            refreshToken = "refresh_token_mock",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = UserInfo(
                id = 2L,
                name = "Admin User",
                email = "admin@example.com",
                roles = setOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
            )
        )

        every { authService.login(loginRequest) } returns expectedResponse

        // Act
        val response = authController.login(loginRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(3, response.body?.user?.roles?.size)
        verify(exactly = 1) { authService.login(loginRequest) }
    }

    @Test
    fun `refreshToken deve retornar novos tokens quando refresh token valido`() {
        // Arrange
        val refreshRequest = RefreshTokenRequest(
            refreshToken = "valid_refresh_token"
        )

        val expectedResponse = RefreshTokenResponse(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            tokenType = "Bearer",
            expiresIn = 3600L
        )

        every { authService.refreshToken(refreshRequest) } returns expectedResponse

        // Act
        val response = authController.refreshToken(refreshRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
        assertEquals("new_access_token", response.body?.accessToken)
        assertEquals("new_refresh_token", response.body?.refreshToken)
        assertEquals("Bearer", response.body?.tokenType)
        assertEquals(3600L, response.body?.expiresIn)
        verify(exactly = 1) { authService.refreshToken(refreshRequest) }
    }

    @Test
    fun `logout deve retornar mensagem de sucesso quando refresh token valido`() {
        // Arrange
        val logoutRequest = LogoutRequest(
            refreshToken = "valid_refresh_token"
        )

        val expectedResponse = MessageResponse(
            message = "Logout successful"
        )

        every { authService.logout(logoutRequest) } returns expectedResponse

        // Act
        val response = authController.logout(logoutRequest)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
        assertEquals("Logout successful", response.body?.message)
        verify(exactly = 1) { authService.logout(logoutRequest) }
    }

    @Test
    fun `me deve retornar perfil do usuario autenticado`() {
        // Arrange
        val userId = 1L
        val userResponse = UserResponse(
            id = userId,
            name = "João Silva",
            email = "joao.silva@example.com",
            roles = setOf("ROLE_USER"),
            createdAt = "2026-01-01T00:00:00",
            updatedAt = "2026-01-01T00:00:00"
        )
        every { securityUtils.getCurrentUserId() } returns userId
        every { userService.getUserById(userId) } returns userResponse

        // Act
        val response = authController.me()

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(userId, response.body?.id)
        assertEquals("João Silva", response.body?.name)
        assertEquals("joao.silva@example.com", response.body?.email)
        assertEquals(setOf("ROLE_USER"), response.body?.roles)
        verify(exactly = 1) { securityUtils.getCurrentUserId() }
        verify(exactly = 1) { userService.getUserById(userId) }
    }
}
