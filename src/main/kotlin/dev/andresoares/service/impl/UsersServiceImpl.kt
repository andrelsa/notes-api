package dev.andresoares.service.impl

import dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dto.UserResponse
import dev.andresoares.dto.UserUpdateRequest
import dev.andresoares.exception.InvalidRoleException
import dev.andresoares.exception.ResourceNotFoundException
import dev.andresoares.model.User
import dev.andresoares.model.UserRole
import dev.andresoares.repository.UserRepository
import dev.andresoares.security.SecurityUtils
import dev.andresoares.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val securityUtils: SecurityUtils
) : UserService {

    override fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toResponse() }
    }

    override fun getAllUsers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { it.toResponse() }
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
            password = passwordEncoder.encode(request.password!!), // Criptografar senha
            roles = mutableSetOf("ROLE_USER") // SECURITY: Always assign ROLE_USER only on registration
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
        request.password?.let { user.password = passwordEncoder.encode(it) } // Criptografar senha
        // SECURITY: Roles should only be updated through dedicated admin endpoints to prevent privilege escalation

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

    @Transactional
    override fun updateUserRoles(userId: Long, roles: Set<String>): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        // Validar se todas as roles são válidas
        val invalidRoles = roles.filter { !UserRole.isValidRole(it) }
        if (invalidRoles.isNotEmpty()) {
            throw InvalidRoleException("Invalid roles: ${invalidRoles.joinToString(", ")}. Valid roles are: ${UserRole.getAllRoleNames().joinToString(", ")}")
        }

        // Garantir que pelo menos ROLE_USER está presente
        val updatedRoles = if (roles.isEmpty()) {
            mutableSetOf("ROLE_USER")
        } else {
            roles.toMutableSet()
        }

        user.roles = updatedRoles

        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    @Transactional
    override fun addRoleToUser(userId: Long, role: String): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        // Validar se a role é válida
        if (!UserRole.isValidRole(role)) {
            throw InvalidRoleException("Invalid role: $role. Valid roles are: ${UserRole.getAllRoleNames().joinToString(", ")}")
        }

        // Adicionar a role (Set não adiciona duplicados automaticamente)
        user.roles.add(role)

        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    @Transactional
    override fun removeRoleFromUser(userId: Long, role: String): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        // Validar se a role é válida antes de tentar remover
        if (!UserRole.isValidRole(role)) {
            throw InvalidRoleException("Invalid role: $role. Valid roles are: ${UserRole.getAllRoleNames().joinToString(", ")}")
        }

        // Verificar se é a última role e se é ROLE_USER
        if (user.roles.size == 1 && user.roles.contains("ROLE_USER")) {
            throw InvalidRoleException("Cannot remove ROLE_USER when it's the only role. User must have at least one role.")
        }

        user.roles.remove(role)

        // Garantir que sempre tenha pelo menos ROLE_USER
        if (user.roles.isEmpty()) {
            user.roles.add("ROLE_USER")
        }

        val updatedUser = userRepository.save(user)
        return updatedUser.toResponse()
    }

    override fun isOwner(userId: Long): Boolean {
        return try {
            securityUtils.getCurrentUserId() == userId
        } catch (e: Exception) {
            false
        }
    }

    private fun User.toResponse() = UserResponse(
        id = id!!,
        name = name,
        email = email,
        roles = roles,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}