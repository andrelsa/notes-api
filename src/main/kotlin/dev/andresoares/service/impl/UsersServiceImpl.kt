package dev.andresoares.dev.andresoares.service.impl

import dev.andresoares.dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dev.andresoares.dto.UserResponse
import dev.andresoares.dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.dev.andresoares.model.User
import dev.andresoares.dev.andresoares.repository.UserRepository
import dev.andresoares.dev.andresoares.service.UserService
import dev.andresoares.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsersServiceImpl(private val userRepository: UserRepository) : UserService {

    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    override fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }
        return user.toResponse()
    }

    override fun searchUsersByName(name: String): List<UserResponse> {
        return userRepository.findByNameContainingIgnoreCase(name).map { it.toResponse() }
    }

    @Transactional
    override fun createUser(request: UserCreateRequest): UserResponse {
        // A validação @NotNull garante que name, email e password não serão null aqui
        val user = User(
            name = request.name!!,
            email = request.email!!,
            password = request.password!!
        )
        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    @Transactional
    override fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User not found with id: $id") }

        request.name?.let { user.name = it }
        request.email?.let { user.email = it }
        request.password?.let { user.password = it }

        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    @Transactional
    override fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User not found with id: $id")
        }
        userRepository.deleteById(id)
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        name = name,
        email = email,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}