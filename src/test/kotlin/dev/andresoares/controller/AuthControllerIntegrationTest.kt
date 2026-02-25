package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dto.LoginRequest
import dev.andresoares.dto.LogoutRequest
import dev.andresoares.dto.RefreshTokenRequest
import dev.andresoares.model.User
import dev.andresoares.repository.RefreshTokenRepository
import dev.andresoares.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        // Limpa os dados antes de cada teste
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `should authenticate user successfully with valid credentials`() {
        // Criar usuário de teste
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").isNumber)
            .andExpect(jsonPath("$.user.id").value(user.id))
            .andExpect(jsonPath("$.user.name").value("João Silva"))
            .andExpect(jsonPath("$.user.email").value("joao.silva@example.com"))
            .andExpect(jsonPath("$.user.roles").isArray)
            .andExpect(jsonPath("$.user.roles[0]").value("ROLE_USER"))
    }

    @Test
    fun `should fail authentication with invalid email`() {
        val loginRequest = LoginRequest(
            email = "naoexiste@example.com",
            password = "Senh@123456"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should fail authentication with invalid password`() {
        // Criar usuário de teste
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "senhaerrada"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should fail authentication with invalid email format`() {
        val loginRequest = mapOf(
            "email" to "emailinvalido",
            "password" to "Senh@123456"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should fail authentication with blank email`() {
        val loginRequest = mapOf(
            "email" to "",
            "password" to "Senh@123456"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should fail authentication with blank password`() {
        val loginRequest = mapOf(
            "email" to "joao.silva@example.com",
            "password" to ""
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should authenticate user with multiple roles`() {
        // Criar usuário com múltiplas roles
        val user = User(
            name = "Admin User",
            email = "admin@example.com",
            password = passwordEncoder.encode("Admin@123456"),
            roles = mutableSetOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "admin@example.com",
            password = "Admin@123456"
        )

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.roles").isArray)
            .andExpect(jsonPath("$.user.roles.length()").value(3))
    }

    @Test
    fun `should refresh token successfully with valid refresh token`() {
        // Criar usuário e fazer login
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        val savedUser = userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login para obter os tokens
        val loginResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val refreshToken = loginResponse.get("refreshToken").asText()

        // Renovar o token
        val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").isNumber)
    }

    @Test
    fun `should fail to refresh token with invalid refresh token`() {
        val refreshRequest = RefreshTokenRequest(
            refreshToken = "invalid_refresh_token"
        )

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should fail to refresh token with blank refresh token`() {
        val refreshRequest = mapOf(
            "refreshToken" to ""
        )

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should logout successfully with valid refresh token`() {
        // Criar usuário e fazer login
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login
        val loginResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val refreshToken = loginResponse.get("refreshToken").asText()

        // Fazer logout
        val logoutRequest = LogoutRequest(refreshToken = refreshToken)

        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Logout successful"))
    }

    @Test
    fun `should fail to logout with invalid refresh token`() {
        val logoutRequest = LogoutRequest(
            refreshToken = "invalid_refresh_token"
        )

        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should fail to logout with blank refresh token`() {
        val logoutRequest = mapOf(
            "refreshToken" to ""
        )

        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should fail to use refresh token after logout`() {
        // Criar usuário e fazer login
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login
        val loginResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val refreshToken = loginResponse.get("refreshToken").asText()

        // Fazer logout
        val logoutRequest = LogoutRequest(refreshToken = refreshToken)
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest))
        )
            .andExpect(status().isOk)

        // Tentar usar o refresh token revogado
        val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should create refresh token in database after successful login`() {
        // Criar usuário
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        val savedUser = userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login
        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)

        // Verificar se o refresh token foi criado no banco
        val refreshTokens = refreshTokenRepository.findByUser(savedUser)
        assertTrue(refreshTokens.isNotEmpty(), "Refresh token should be created in database")
        assertFalse(refreshTokens[0].revoked, "Refresh token should not be revoked")
    }

    @Test
    fun `should revoke old refresh token when creating new one during refresh`() {
        // Criar usuário e fazer login
        val user = User(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = passwordEncoder.encode("Senh@123456"),
            roles = mutableSetOf("ROLE_USER")
        )
        val savedUser = userRepository.save(user)

        val loginRequest = LoginRequest(
            email = "joao.silva@example.com",
            password = "Senh@123456"
        )

        // Fazer login
        val loginResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val oldRefreshToken = loginResponse.get("refreshToken").asText()

        Thread.sleep(1000)

        // Renovar token
        val refreshRequest = RefreshTokenRequest(refreshToken = oldRefreshToken)
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)

        // Verificar se o token antigo foi revogado
        val oldToken = refreshTokenRepository.findByToken(oldRefreshToken)
        assertTrue(oldToken.isPresent, "Old refresh token should exist in database")
        assertTrue(oldToken.get().revoked, "Old refresh token should be revoked")
    }
}

