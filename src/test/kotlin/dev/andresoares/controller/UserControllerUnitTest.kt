package dev.andresoares.controller

import dev.andresoares.controller.UserController
import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dto.UserResponse
import dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.service.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus

/**
 * Teste unitário do UserController usando MockK.
 *
 * Este teste demonstra os benefícios de usar interfaces:
 * - Facilita criação de mocks
 * - Testa o controller isoladamente
 * - Não depende da implementação real do serviço
 * - Execução rápida (não precisa subir o Spring Context)
 */
class UserControllerUnitTest {

    private lateinit var userService: UserService
    private lateinit var userController: UserController

    @BeforeEach
    fun setup() {
        // Criando mock da interface UserService
        userService = mockk<UserService>()
        userController = UserController(userService)
    }

    @Test
    fun `getAllUsers deve retornar lista de usuarios`() {
        // Arrange
        val expectedUsers = listOf(
            UserResponse(1L, "João Silva", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T10:00:00"),
            UserResponse(2L, "Maria Santos", "maria@example.com", "2026-02-03T11:00:00", "2026-02-03T11:00:00")
        )
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"))
        val page = PageImpl(expectedUsers, pageable, expectedUsers.size.toLong())

        every { userService.getAllUsers(any()) } returns page

        // Act
        val response = userController.getAllUsers(null, 0, 20, "id", "asc")

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 1) { userService.getAllUsers(any()) }
    }

    @Test
    fun `getAllUsers deve buscar usuarios por nome quando filtro e fornecido`() {
        // Arrange
        val name = "João"
        val expectedUsers = listOf(
            UserResponse(1L, "João Silva", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T10:00:00")
        )
        every { userService.searchUsersByName(name) } returns expectedUsers

        // Act
        val response = userController.getAllUsers(name, 0, 20, "id", "asc")

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        verify(exactly = 1) { userService.searchUsersByName(name) }
    }

    @Test
    fun `getUserById deve retornar usuario quando id existe`() {
        // Arrange
        val userId = 1L
        val expectedUser = UserResponse(userId, "João Silva", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T10:00:00")
        every { userService.getUserById(userId) } returns expectedUser

        // Act
        val response = userController.getUserById(userId)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.getUserById(userId) }
    }

    @Test
    fun `createUser deve retornar usuario criado com status CREATED`() {
        // Arrange
        val request = UserCreateRequest(
            name = "João Silva",
            email = "joao@example.com",
            password = "senha123456"
        )
        val expectedUser = UserResponse(1L, "João Silva", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T10:00:00")
        every { userService.createUser(request) } returns expectedUser

        // Act
        val response = userController.createUser(request)

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.createUser(request) }
    }

    @Test
    fun `updateUser deve retornar usuario atualizado`() {
        // Arrange
        val userId = 1L
        val request = UserUpdateRequest(
            name = "João Silva Atualizado",
            email = "joao.novo@example.com",
            password = "novaSenha123"
        )
        val expectedUser = UserResponse(userId, "João Silva Atualizado", "joao.novo@example.com", "2026-02-03T10:00:00", "2026-02-03T11:00:00")
        every { userService.updateUser(userId, request) } returns expectedUser

        // Act
        val response = userController.updateUser(userId, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.updateUser(userId, request) }
    }

    @Test
    fun `updateUser deve permitir atualizacao parcial - apenas nome`() {
        // Arrange
        val userId = 1L
        val request = UserUpdateRequest(name = "João Atualizado", email = null, password = null)
        val expectedUser = UserResponse(userId, "João Atualizado", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T11:00:00")
        every { userService.updateUser(userId, request) } returns expectedUser

        // Act
        val response = userController.updateUser(userId, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.updateUser(userId, request) }
    }

    @Test
    fun `updateUser deve permitir atualizacao parcial - apenas email`() {
        // Arrange
        val userId = 1L
        val request = UserUpdateRequest(name = null, email = "novo@example.com", password = null)
        val expectedUser = UserResponse(userId, "João Silva", "novo@example.com", "2026-02-03T10:00:00", "2026-02-03T11:00:00")
        every { userService.updateUser(userId, request) } returns expectedUser

        // Act
        val response = userController.updateUser(userId, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.updateUser(userId, request) }
    }

    @Test
    fun `updateUser deve permitir atualizacao parcial - apenas senha`() {
        // Arrange
        val userId = 1L
        val request = UserUpdateRequest(name = null, email = null, password = "novaSenha123")
        val expectedUser = UserResponse(userId, "João Silva", "joao@example.com", "2026-02-03T10:00:00", "2026-02-03T11:00:00")
        every { userService.updateUser(userId, request) } returns expectedUser

        // Act
        val response = userController.updateUser(userId, request)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedUser, response.body)
        verify(exactly = 1) { userService.updateUser(userId, request) }
    }

    @Test
    fun `deleteUser deve retornar NO_CONTENT quando usuario e deletado`() {
        // Arrange
        val userId = 1L
        every { userService.deleteUser(userId) } returns Unit

        // Act
        val response = userController.deleteUser(userId)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify(exactly = 1) { userService.deleteUser(userId) }
    }
}
