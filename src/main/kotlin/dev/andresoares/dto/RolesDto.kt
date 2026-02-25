package dev.andresoares.dto

import jakarta.validation.constraints.NotEmpty

/**
 * Request para atualizar roles de um usuário.
 * Usado por administradores para gerenciar permissões.
 */
data class UserRolesRequest(
    @field:NotEmpty(message = "Roles list cannot be empty")
    val roles: Set<String>
)

/**
 * Request para adicionar/remover uma role específica.
 */
data class SingleRoleRequest(
    @field:NotEmpty(message = "Role cannot be empty")
    val role: String
)
