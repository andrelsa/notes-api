package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should add role to user successfully`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Add ROLE_ADMIN
        mockMvc.perform(
            post("/api/v1/admin/users/$userId/roles/ROLE_ADMIN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles.length()").value(2))
            .andExpect(jsonPath("$.roles[?(@=='ROLE_USER')]").exists())
            .andExpect(jsonPath("$.roles[?(@=='ROLE_ADMIN')]").exists())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail to add invalid role to user`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Try to add invalid role
        mockMvc.perform(
            post("/api/v1/admin/users/$userId/roles/ROLE_INVALID")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Invalid role: ROLE_INVALID. Valid roles are: ROLE_USER, ROLE_ADMIN, ROLE_MANAGER, ROLE_VIEWER"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should remove role from user successfully`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Add ROLE_ADMIN
        mockMvc.perform(
            post("/api/v1/admin/users/$userId/roles/ROLE_ADMIN")
        )
            .andExpect(status().isOk)

        // Remove ROLE_ADMIN
        mockMvc.perform(
            delete("/api/v1/admin/users/$userId/roles/ROLE_ADMIN")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles.length()").value(1))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail to remove invalid role from user`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Try to remove invalid role - SHOULD FAIL WITH 400 BAD REQUEST
        mockMvc.perform(
            delete("/api/v1/admin/users/$userId/roles/ROLE_INVALID")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Invalid role: ROLE_INVALID. Valid roles are: ROLE_USER, ROLE_ADMIN, ROLE_MANAGER, ROLE_VIEWER"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail to remove ROLE_USER when it is the only role`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Try to remove ROLE_USER when it's the only role
        mockMvc.perform(
            delete("/api/v1/admin/users/$userId/roles/ROLE_USER")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Cannot remove ROLE_USER when it's the only role. User must have at least one role."))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should update all user roles successfully`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Update roles
        val rolesRequest = mapOf("roles" to listOf("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER"))

        mockMvc.perform(
            put("/api/v1/admin/users/$userId/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rolesRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles.length()").value(3))
            .andExpect(jsonPath("$.roles[?(@=='ROLE_USER')]").exists())
            .andExpect(jsonPath("$.roles[?(@=='ROLE_ADMIN')]").exists())
            .andExpect(jsonPath("$.roles[?(@=='ROLE_MANAGER')]").exists())
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail to update user roles with invalid role`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Try to update with invalid role
        val rolesRequest = mapOf("roles" to listOf("ROLE_USER", "ROLE_INVALID"))

        mockMvc.perform(
            put("/api/v1/admin/users/$userId/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rolesRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Invalid roles: ROLE_INVALID. Valid roles are: ROLE_USER, ROLE_ADMIN, ROLE_MANAGER, ROLE_VIEWER"))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `should deny access to admin endpoints for non-admin users`() {
        // Create user first (with admin privileges for creation)
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
        )

        // This will work because we're creating through public endpoint
        val createResult = mockMvc.perform(
            post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val userId = response.get("id").asLong()

        // Now try to add role as non-admin user - should fail
        mockMvc.perform(
            post("/api/v1/admin/users/$userId/roles/ROLE_ADMIN")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should get all user roles`() {
        // Create user first
        val createRequest = UserCreateRequest(
            name = "Test User",
            email = "test@example.com",
            password = "Test#123"
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

        // Get user details (which includes roles) through the regular user endpoint
        mockMvc.perform(
            get("/api/v1/users/$userId")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.roles").isArray)
            .andExpect(jsonPath("$.roles.length()").value(1))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail when trying to add role to non-existent user`() {
        mockMvc.perform(
            post("/api/v1/admin/users/99999/roles/ROLE_ADMIN")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with id: 99999"))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `should fail when trying to remove role from non-existent user`() {
        mockMvc.perform(
            delete("/api/v1/admin/users/99999/roles/ROLE_ADMIN")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("User not found with id: 99999"))
    }
}

