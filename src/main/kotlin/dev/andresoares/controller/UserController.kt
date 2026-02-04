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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userService: UserService) {

    @GetMapping
    fun getAllUsers(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): ResponseEntity<*> {
        // Se há filtro de nome, retorna lista sem paginação
        if (name != null) {
            val users = userService.searchUsersByName(name)
            return ResponseEntity.ok(users)
        }

        // Sem filtro, retorna com paginação
        val sortDirection = if (direction.equals("desc", ignoreCase = true)) {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        val usersPage: Page<UserResponse> = userService.getAllUsers(pageable)
        return ResponseEntity.ok(usersPage)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping
    fun createUser(@Valid @RequestBody request: UserCreateRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
