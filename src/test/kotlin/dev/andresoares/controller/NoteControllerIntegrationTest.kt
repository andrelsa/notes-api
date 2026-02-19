package dev.andresoares.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.andresoares.dto.NoteCreateRequest
import dev.andresoares.dto.NoteUpdateRequest
import dev.andresoares.model.User
import dev.andresoares.repository.NoteRepository
import dev.andresoares.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com", roles = ["USER"])
class NoteControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var noteRepository: NoteRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        // Limpa o banco de dados antes de cada teste
        noteRepository.deleteAll()
        userRepository.deleteAll()

        // Cria um usuário padrão para os testes
        userRepository.save(
            User(
                name = "Test User",
                email = "test@notesapi.com",
                password = "Test@123"
            )
        )
    }

    @Test
    fun `should create and retrieve a note`() {
        val createRequest = NoteCreateRequest(
            title = "Test Note",
            content = "This is a test note content"
        )

        // Create note
        val createResult = mockMvc.perform(
            post("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("Test Note"))
            .andExpect(jsonPath("$.content").value("This is a test note content"))
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val noteId = response.get("id").asLong()

        // Retrieve note
        mockMvc.perform(get("/api/v1/notes/$noteId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(noteId))
            .andExpect(jsonPath("$.title").value("Test Note"))
    }

    @Test
    fun `should update a note`() {
        val createRequest = NoteCreateRequest(
            title = "Original Title",
            content = "Original Content"
        )

        // Create note
        val createResult = mockMvc.perform(
            post("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val noteId = response.get("id").asLong()

        val updateRequest = NoteUpdateRequest(
            title = "Updated Title",
            content = "Updated Content"
        )

        // Update note
        mockMvc.perform(
            patch("/api/v1/notes/$noteId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.content").value("Updated Content"))
    }

    @Test
    fun `should delete a note`() {
        val createRequest = NoteCreateRequest(
            title = "Note to Delete",
            content = "This note will be deleted"
        )

        // Create note
        val createResult = mockMvc.perform(
            post("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readTree(createResult.response.contentAsString)
        val noteId = response.get("id").asLong()

        // Delete note
        mockMvc.perform(delete("/api/v1/notes/$noteId"))
            .andExpect(status().isNoContent)

        // Verify note is deleted
        mockMvc.perform(get("/api/v1/notes/$noteId"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return validation error when title is blank`() {
        val createRequest = mapOf("title" to "", "content" to "Content")

        mockMvc.perform(
            post("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.validationErrors").exists())
    }

    @Test
    fun `should search notes by title`() {
        // Create test notes
        val note1 = NoteCreateRequest(title = "Spring Boot Tutorial", content = "Content 1")
        val note2 = NoteCreateRequest(title = "Kotlin Guide", content = "Content 2")
        val note3 = NoteCreateRequest(title = "Spring Data JPA", content = "Content 3")

        listOf(note1, note2, note3).forEach { request ->
            mockMvc.perform(
                post("/api/v1/notes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        // Search for notes with "Spring" in title
        mockMvc.perform(get("/api/v1/notes?title=Spring"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
}
