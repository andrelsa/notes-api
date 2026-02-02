package dev.andresoares.exception.handler

import dev.andresoares.exception.*
import dev.andresoares.exception.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private lateinit var handler: GlobalExceptionHandler

    @Mock
    private lateinit var request: HttpServletRequest

    @Test
    fun `should handle ResourceNotFoundException with 404 status`() {
        // Given
        val exception = ResourceNotFoundException("Note not found with id: 123")
        `when`(request.requestURI).thenReturn("/api/notes/123")

        // When
        val response = handler.handleResourceNotFoundException(exception, request)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals(404, response.body?.status)
        assertEquals("Note not found with id: 123", response.body?.message)
        assertEquals("/api/notes/123", response.body?.path)
        assertNotNull(response.body?.traceId)
    }

    @Test
    fun `should handle ValidationException with 400 status`() {
        // Given
        val errors = mapOf(
            "title" to listOf("Title is required"),
            "content" to listOf("Content cannot be empty")
        )
        val exception = InvalidInputException("Validation failed", errors)
        `when`(request.requestURI).thenReturn("/api/notes")

        // When
        val response = handler.handleValidationException(exception, request)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals(400, response.body?.status)
        assertEquals("Validation failed", response.body?.message)
        assertEquals(errors, response.body?.validationErrors)
    }

    @Test
    fun `should handle BusinessRuleViolationException with 422 status`() {
        // Given
        val exception = BusinessRuleViolationException("Cannot delete note with active references")
        `when`(request.requestURI).thenReturn("/api/notes/123")

        // When
        val response = handler.handleBusinessRuleViolationException(exception, request)

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
        assertNotNull(response.body)
        assertEquals(422, response.body?.status)
        assertEquals("Cannot delete note with active references", response.body?.message)
    }

    @Test
    fun `should handle ResourceAlreadyExistsException with 409 status`() {
        // Given
        val exception = ResourceAlreadyExistsException("Note already exists")
        `when`(request.requestURI).thenReturn("/api/notes")

        // When
        val response = handler.handleResourceAlreadyExistsException(exception, request)

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNotNull(response.body)
        assertEquals(409, response.body?.status)
    }

    @Test
    fun `should handle generic Exception with 500 status`() {
        // Given
        val exception = RuntimeException("Unexpected error")
        `when`(request.requestURI).thenReturn("/api/notes")

        // When
        val response = handler.handleGenericException(exception, request)

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals(500, response.body?.status)
        assertNotNull(response.body?.traceId)
        assertNotNull(response.body?.details)
    }
}
