package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.model.User
import dev.andresoares.repository.NoteRepository
import dev.andresoares.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
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

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    // Email que corresponde ao @WithMockUser(username = ...) nos testes que verificam ownership
    private val adminEmail = "admin@example.com"

    @BeforeEach
    fun setUp() {
        noteRepository.deleteAll()
        userRepository.deleteAll()
    }

    // Cria um usuário ADMIN no banco para testes que usam isOwner / ADMIN role
    private fun createAdminUser(): User {
        return userRepository.save(
            User(
                name = "Admin User",
                email = adminEmail,
                password = passwordEncoder.encode("Admin@123"),
                roles = mutableSetOf("ROLE_ADMIN")
            )
        )
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should create and retrieve a user`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
            password = "Abc#123def"
        )

        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        mockMvc.perform(get("/api/v1/users/$userId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should update a user completely`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
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

        val updateRequest = UserUpdateRequest(
            name = "João Silva Atualizado",
            email = "joao.novo@example.com",
            password = "Abc#123def"
        )

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva Atualizado"))
            .andExpect(jsonPath("$.email").value("joao.novo@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should update user partially - only name`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
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

        val updateRequest = mapOf("name" to "João Atualizado")

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Atualizado"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should update user partially - only email`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
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

        val updateRequest = mapOf("email" to "novo.email@example.com")

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("novo.email@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should update user partially - only password`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
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

        val updateRequest = mapOf("password" to "Abc#123def")

        mockMvc.perform(
            patch("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("João Silva"))
            .andExpect(jsonPath("$.email").value("joao.silva@example.com"))
            .andExpect(jsonPath("$.roles").isArray)
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should delete a user`() {
        createAdminUser()

        val createRequest = UserCreateRequest(
            name = "João Silva",
            email = "joao.silva@example.com",
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

        mockMvc.perform(delete("/api/v1/users/$userId"))
            .andExpect(status().isNoContent)

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
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should search users by name`() {
        createAdminUser()

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

        mockMvc.perform(get("/api/v1/users?name=João"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should return empty list when searching for non-existent name`() {
        createAdminUser()

        val user = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "Abc#123def")
        mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
        )

        mockMvc.perform(get("/api/v1/users?name=Inexistente"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should list all users`() {
        createAdminUser()

        val user1 = UserCreateRequest(name = "João Silva", email = "joao@example.com", password = "Abc#123def")
        val user2 = UserCreateRequest(name = "Maria Santos", email = "maria@example.com", password = "Abc#123def")

        listOf(user1, user2).forEach { request ->
            mockMvc.perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        // Total: admin + user1 + user2 = 3
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.totalElements").value(3))
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = ["USER"])
    fun `should return 403 when user tries to list all users`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isForbidden)
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
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
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
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should reject update with password containing spaces`() {
        createAdminUser()

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
    @WithMockUser(username = "admin@example.com", roles = ["ADMIN"])
    fun `should reject update with weak password`() {
        createAdminUser()

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
