package dev.andresoares.controller

import dev.andresoares.dto.UserResponse
import dev.andresoares.dto.UserRolesRequest
import dev.andresoares.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Controller de administração para gerenciar usuários e roles.
 *
 * Todos os endpoints deste controller requerem ROLE_ADMIN.
 *
 * Endpoints disponíveis:
 * - PUT /api/v1/admin/users/{id}/roles - Substituir todas as roles de um usuário
 * - POST /api/v1/admin/users/{id}/roles/{role} - Adicionar uma role a um usuário
 * - DELETE /api/v1/admin/users/{id}/roles/{role} - Remover uma role de um usuário
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userService: UserService
) {

    /**
     * Atualizar todas as roles de um usuário (substitui as existentes).
     *
     * PUT /api/v1/admin/users/{id}/roles
     *
     * Body:
     * {
     *   "roles": ["ROLE_USER", "ROLE_MANAGER"]
     * }
     *
     * @param id ID do usuário
     * @param request Request contendo as novas roles
     * @return Usuário atualizado com as novas roles
     */
    @PutMapping("/{id}/roles")
    fun updateUserRoles(
        @PathVariable id: Long,
        @Valid @RequestBody request: UserRolesRequest
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUserRoles(id, request.roles)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Adicionar uma role a um usuário (mantém as roles existentes).
     *
     * POST /api/v1/admin/users/{id}/roles/{role}
     *
     * Exemplo: POST /api/v1/admin/users/1/roles/ROLE_ADMIN
     *
     * @param id ID do usuário
     * @param role Role a ser adicionada (ex: ROLE_ADMIN, ROLE_MANAGER)
     * @return Usuário atualizado com a nova role
     */
    @PostMapping("/{id}/roles/{role}")
    fun addRoleToUser(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.addRoleToUser(id, role)
        return ResponseEntity.ok(updatedUser)
    }

    /**
     * Remover uma role de um usuário.
     *
     * DELETE /api/v1/admin/users/{id}/roles/{role}
     *
     * Exemplo: DELETE /api/v1/admin/users/1/roles/ROLE_MANAGER
     *
     * Nota: Não é possível remover ROLE_USER se for a única role do usuário.
     *
     * @param id ID do usuário
     * @param role Role a ser removida
     * @return Usuário atualizado sem a role removida
     */
    @DeleteMapping("/{id}/roles/{role}")
    fun removeRoleFromUser(
        @PathVariable id: Long,
        @PathVariable role: String
    ): ResponseEntity<UserResponse> {
        val updatedUser = userService.removeRoleFromUser(id, role)
        return ResponseEntity.ok(updatedUser)
    }
}
