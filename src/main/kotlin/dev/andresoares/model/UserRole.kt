package dev.andresoares.model

/**
 * Enum que define as roles disponíveis no sistema.
 *
 * Usar este enum garante consistência e evita erros de digitação
 * ao atribuir roles aos usuários.
 */
enum class UserRole(val roleName: String) {
    /**
     * Usuário padrão - Pode criar e gerenciar suas próprias notas
     */
    USER("ROLE_USER"),

    /**
     * Administrador - Acesso total ao sistema
     * - Pode gerenciar todos os usuários
     * - Pode gerenciar todas as notas
     * - Pode atribuir/remover roles
     */
    ADMIN("ROLE_ADMIN"),

    /**
     * Gerente - Acesso intermediário
     * - Pode visualizar todas as notas
     * - Pode gerenciar suas próprias notas
     * - NÃO pode deletar usuários
     */
    MANAGER("ROLE_MANAGER"),

    /**
     * Visualizador - Apenas leitura
     * - Pode visualizar notas compartilhadas
     * - NÃO pode criar/editar/deletar
     */
    VIEWER("ROLE_VIEWER");

    companion object {
        /**
         * Retorna o enum UserRole a partir do nome da role
         */
        fun fromString(role: String): UserRole? {
            return values().find { it.roleName == role }
        }

        /**
         * Retorna todas as roles como Set de Strings
         */
        fun getAllRoleNames(): Set<String> {
            return values().map { it.roleName }.toSet()
        }

        /**
         * Valida se uma role string é válida
         */
        fun isValidRole(role: String): Boolean {
            return values().any { it.roleName == role }
        }
    }
}
