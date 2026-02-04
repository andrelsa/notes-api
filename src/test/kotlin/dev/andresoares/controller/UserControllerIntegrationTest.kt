package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.dev.andresoares.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        // Limpa o banco de dados antes de cada teste
        userRepository.deleteAll()
    }

    @Test
    fun `should create and retrieve a user`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        // Retrieve user
        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
    }

    @Test
    fun `should update a user completely`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        val updateRequest = UserUpdateRequest(
            name = "João Silva Atualizado",
            email = "joao.novo@example.com",
            password = "novaSenha123"
        )

        // Update user
        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva Atualizado"))
            .andExpect(jsonPath("$.email").value("joao.novo@example.com"))
    }

    @Test
    fun `should update user partially - only name`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        val updateRequest = mapOf("name" to "João Atualizado")

        // Update user - only name
        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Atualizado"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com")) // Email não mudou
    }

    @Test
    fun `should update user partially - only email`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        val updateRequest = mapOf("email" to "novo.email@example.com")

        // Update user - only email
        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva")) // Nome não mudou
            .andExpect(jsonPath("$.email").value("novo.email@example.com"))
    }

    @Test
    fun `should update user partially - only password`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        val updateRequest = mapOf("password" to "novaSenha456")

        // Update user - only password
        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva")) // Nome não mudou
            .andExpect(jsonPath("$.email").value("joao.silva@example.com")) // Email não mudou
    }

    @Test
    fun `should delete a user`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "senha123456"
        )

        // Create user
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        // Delete user
        mockMvc.perform(delete("/api/v1/users/$userId"))
            .andExpect(status().isNoContent)

        // Verify user is deleted
        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return validation error when name is blank`() {
        val createRequest = mapOf(
            "name" to "",
            "email" to "joao@example.com",
            "password" to "senha123456"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").exists())
    }

    @Test
    fun `should return validation error when email is invalid`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "email-invalido",
            "password" to "senha123456"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.validationErrors.email").exists())
    }

    @Test
    fun `should return validation error when password is too short`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "123"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.validationErrors.password").exists())
    }

    @Test
    fun `should return validation error when name is too short`() {
        val createRequest = mapOf(
            "name" to "Jo",
            "email" to "joao@example.com",
            "password" to "senha123456"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.validationErrors.name").exists())
    }

    @Test
    fun `should search users by name`() {
        // Create test users
        val user1 = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "senha123456")
        val user2 = UserCreateRequest(name = "Maria Santos", email = "maria@example.com", password = "senha123456")
        val user3 = UserCreateRequest(name = "João Pedro", email = "joaop@example.com", password = "senha123456")

        listOf(user1, user2, user3).forEach { request ->
            mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        // Search for users with "João" in name
        mockMvc.perform(get("/api/v1/users?name=João"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should return empty list when searching for non-existent name`() {
        // Create test user
        val user = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "senha123456")
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
        )

        // Search for non-existent name
        mockMvc.perform(get("/api/v1/users?name=Inexistente"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `should list all users`() {
        // Create test users
        val user1 = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "senha123456")
        val user2 = UserCreateRequest(name = "Maria Santos", email = "maria@example.com", password = "senha123456")

        listOf(user1, user2).forEach { request ->
            mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        // List all users (retorna Page)
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
    }
}
