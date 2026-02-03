package dev.andresoares.dev.andresoares.dto

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
    var password: String?
)

data class UserUpdateRequest(
    @field:Size(min = 3, max = 255, message = "Field 'name' must be between 3 and 255 characters when provided")
    val name: String?,

    @field:Email(message = "Invalid email format")
    val email: String?,

    @field:Size(min = 8, max = 15, message = "Field 'password' must be between 8 and 15 characters when provided")
    val password: String?
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
)
