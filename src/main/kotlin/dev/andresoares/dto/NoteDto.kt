package dev.andresoares.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class NoteCreateRequest(
    @field:NotNull(message = "Field 'title' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'title' cannot be empty or blank")
    @field:Size(min = 1, max = 255, message = "Field 'title' must be between 1 and 255 characters")
    var title: String?,

    @field:NotNull(message = "Field 'content' is required and must be provided in the request body")
    @field:NotBlank(message = "Field 'content' cannot be empty or blank")
    @field:Size(min = 1, max = 5000, message = "Field 'content' must be between 1 and 5000 characters")
    var content: String?
)

data class NoteUpdateRequest(
    @field:Size(min = 1, max = 255, message = "Field 'title' must be between 1 and 255 characters when provided")
    val title: String?,

    @field:Size(min = 1, max = 5000, message = "Field 'content' must be between 1 and 5000 characters when provided")
    val content: String?
)

data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)
