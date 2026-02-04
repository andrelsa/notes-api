package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.repository.UserRepository
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
            password = "Abc#123def"
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
            password = "Abc#123def"
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
            password = "Abc#123def"
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
            password = "Abc#123def"
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
            password = "Abc#123def"
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
            password = "Abc#123def"
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

        val updateRequest = mapOf("password" to "Abc#123def")

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
            password = "Abc#123def"
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
            "password" to "Abc#123def"
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
            "password" to "Abc#123def"
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
            "password" to "Abc#123def"
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
        val user1 = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "Abc#123def")
        val user2 = UserCreateRequest(name = "Maria Santos", email = "maria@example.com", password = "Abc#123def")
        val user3 = UserCreateRequest(name = "João Pedro", email = "joaop@example.com", password = "Abc#123def")

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
        val user = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "Abc#123def")
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
        val user1 = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "Abc#123def")
        val user2 = UserCreateRequest(name = "Maria Santos", email = "maria@example.com", password = "Abc#123def")

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

    // ==================== TESTES DE VALIDAÇÃO DE SENHA ====================

    @Test
    fun `should reject password with space at the beginning`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to " Abc#123def"
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
    fun `should reject password with space at the end`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "Abc#123def "
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
    fun `should reject password with space in the middle`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "Abc #123def"
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
    fun `should reject password with multiple spaces`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "Abc  #123  def"
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
    fun `should reject password without letters`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "12345678#"
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
    fun `should reject password without numbers`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "AbcDefgh#"
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
    fun `should reject password without special characters`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao@example.com",
            "password" to "Abc12345678"
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
    fun `should accept valid password with all requirements`() {
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.valid@example.com",
            password = "Abc#123def"
        )

        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.valid@example.com"))
    }

    @Test
    fun `should accept password with different special characters`() {
        val passwords = listOf("Pass@123", "Pass!123", "Pass#123", "Pass\$123", "Pass%123", "Pass&123")

        passwords.forEachIndexed { index, password ->
            val createRequest = UserCreateRequest(
                name = "User $index",
                email = "user$index@example.com",
                password = password
            )

            mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
                .andExpect(status().isCreated)
        }
    }

    @Test
    fun `should reject password that is too long`() {
        val createRequest = mapOf(
            "name" to "João Silva",
            "email" to "joao.long@example.com",
            "password" to "Abc#1234567890123" // 17 caracteres (máximo é 15)
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
    fun `should reject update with password containing spaces`() {
        // Create user with valid password
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.update1@example.com",
            password = "Abc#123def"
        )

        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        // Try to update with invalid password (with spaces)
        val updateRequest = mapOf("password" to "Abc #123def")

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.validationErrors.password").exists())
    }

    @Test
    fun `should reject update with weak password`() {
        // Create user with valid password
        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.update2@example.com",
            password = "Abc#123def"
        )

        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        // Try to update with weak password (no special character)
        val updateRequest = mapOf("password" to "Abc123def")

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.validationErrors.password").exists())
    }
}
