package dev.andresoares.dto

import dev.andresoares.validation.NoWhitespace
import dev.andresoares.validation.ValidPassword
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class UserCreateRequest(
    @field:NotNull(message = "Field 'name' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'name' cannot be empty or blank")
    @field:Size(min = 3, max = 255, message = "Field 'name' must be between 3 and 255 characters")
    var name: String?,

    @field:NotNull(message = "Field 'email' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'email' cannot be empty or blank")
    @field:Size(min = 1, max = 5000, message = "Field 'email' must be between 1 and 5000 characters")
    @field:Email(message = "Invalid email format")
    var email: String?,

    @field:NotNull(message = "Field 'password' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'password' cannot be empty or blank")
    @field:Size(min = 8, max = 15, message = "Field 'password' must be between 8 and 15 characters")
    @field:ValidPassword(message = "Field 'password' must contain at least one letter, one number, and one special character")
    @field:NoWhitespace(message = "Field 'password' cannot contain whitespace characters")
    var password: String?,

    // Roles opcionais - se não fornecido, será atribuído ROLE_USER por padrão
    val roles: Set<String>? = null
)

data class UserUpdateRequest(
    @field:Size(min = 3, max = 255, message = "Field 'name' must be between 3 and 255 characters when provided")
    val name: String?,

    @field:Email(message = "Invalid email format")
    val email: String?,

    @field:Size(min = 8, max = 15, message = "Field 'password' must be between 8 and 15 characters when provided")
    @field:ValidPassword(message = "Field 'password' must contain at least one letter, one number, and one special character")
    @field:NoWhitespace(message = "Field 'password' cannot contain whitespace characters")
    val password: String?,

    // Roles opcionais para atualização (apenas admins devem poder atualizar)
    val roles: Set<String>? = null
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val roles: Set<String>,
    val createdAt: String,
    val updatedAt: String
)
