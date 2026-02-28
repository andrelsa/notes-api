package dev.andresoares.controller

import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dto.UserResponse
import dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.service.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Controller de usuários com controle de autorização baseado em roles.
 *
 * Regras de acesso:
 * - GET /api/v1/users       → ADMIN (lista todos os usuários)
 * - GET /api/v1/users/{id}  → ADMIN ou próprio usuário
 * - POST /api/v1/users      → público (cadastro)
 * - PATCH /api/v1/users/{id}→ ADMIN ou próprio usuário
 * - DELETE /api/v1/users/{id}→ ADMIN apenas
 */
@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    /**
     * Lista todos os usuários.
     * Apenas ADMIN pode listar todos os usuários.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<*> {
        if (name != null) {
            val users = userService.searchUsersByName(name)
            return ResponseEntity.ok(users)
        }

        val sortDirection = if (direction.equals("desc", ignoreCase = true)) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        val usersPage: Page<UserResponse> = userService.getAllUsers(pageable)
        return ResponseEntity.ok(usersPage)
    }

    /**
     * Busca um usuário por ID.
     * ADMIN pode ver qualquer usuário.
     * Usuário autenticado pode ver seu próprio perfil.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id)")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    /**
     * Cria (cadastra) um novo usuário.
     * Endpoint público — sem autenticação.
     */
    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    /**
     * Atualiza um usuário existente (nome, email, senha).
     * ADMIN pode atualizar qualquer usuário.
     * Usuário autenticado pode atualizar apenas seus próprios dados.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id)")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }

    /**
     * Deleta um usuário.
     * Apenas ADMIN pode deletar usuários.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
