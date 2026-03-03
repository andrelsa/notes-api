package dev.andresoares.controller

import dev.andresoares.dto.*
import dev.andresoares.security.AuthenticatedUser
import dev.andresoares.service.AuthService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AuthControllerUnitTest {

    private lateinit var authService: AuthService
    private lateinit var authController: AuthController

    @BeforeEach
    fun setup() {
        authService = mockk<AuthService>()
        authController = AuthController(authService)
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `login deve retornar token JWT quando credenciais sao validas`() {
        val loginRequest = LoginRequest(email = "joao.silva@example.com", password = "senha123456")
        val expectedResponse = LoginResponse(
            accessToken = "eyJhbGciOiJIUzUxMiJ9...",
            refreshToken = "refresh_token_mock",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = UserInfo(id = 1L, name = "João Silva", email = "joao.silva@example.com", roles = setOf("ROLE_USER"))
        )
        every { authService.login(loginRequest) } returns expectedResponse

        val response = authController.login(loginRequest)

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
        val loginRequest = LoginRequest(email = "admin@example.com", password = "admin123456")
        val expectedResponse = LoginResponse(
            accessToken = "eyJhbGciOiJIUzUxMiJ9...",
            refreshToken = "refresh_token_mock",
            tokenType = "Bearer",
            expiresIn = 3600L,
            user = UserInfo(id = 2L, name = "Admin User", email = "admin@example.com",
                roles = setOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER"))
        )
        every { authService.login(loginRequest) } returns expectedResponse

        val response = authController.login(loginRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(3, response.body?.user?.roles?.size)
        verify(exactly = 1) { authService.login(loginRequest) }
    }

    @Test
    fun `refreshToken deve retornar novos tokens quando refresh token valido`() {
        val refreshRequest = RefreshTokenRequest(refreshToken = "valid_refresh_token")
        val expectedResponse = RefreshTokenResponse(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            tokenType = "Bearer",
            expiresIn = 3600L
        )
        every { authService.refreshToken(refreshRequest) } returns expectedResponse

        val response = authController.refreshToken(refreshRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("new_access_token", response.body?.accessToken)
        assertEquals("new_refresh_token", response.body?.refreshToken)
        verify(exactly = 1) { authService.refreshToken(refreshRequest) }
    }

    @Test
    fun `logout deve retornar mensagem de sucesso quando refresh token valido`() {
        val logoutRequest = LogoutRequest(refreshToken = "valid_refresh_token")
        every { authService.logout(logoutRequest) } returns MessageResponse("Logout successful")

        val response = authController.logout(logoutRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("Logout successful", response.body?.message)
        verify(exactly = 1) { authService.logout(logoutRequest) }
    }

    @Test
    fun `me deve retornar perfil do usuario autenticado sem query ao banco`() {
        // Simula o AuthenticatedUser no SecurityContext (como faz o JwtAuthenticationFilter)
        val authenticatedUser = AuthenticatedUser(
            id = 1L,
            email = "joao.silva@example.com",
            name = "João Silva",
            roles = setOf("ROLE_USER")
        )
        val auth = UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.authorities)
        SecurityContextHolder.getContext().authentication = auth

        val response = authController.me()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1L, response.body?.id)
        assertEquals("João Silva", response.body?.name)
        assertEquals("joao.silva@example.com", response.body?.email)
        assertEquals(setOf("ROLE_USER"), response.body?.roles)
        // Confirma que nenhum service foi chamado (zero DB queries extras)
        verify(exactly = 0) { authService.login(any()) }
    }
}


/**
 * Teste unitário do AuthController usando MockK.
 *
 * Este teste demonstra os benefícios de usar interfaces:
 * - Facilita criação de mocks
 * - Testa o controller isoladamente
 * - Não depende da implementação real do serviço
 * - Execução rápida (não precisa subir o Spring Context)
 */
