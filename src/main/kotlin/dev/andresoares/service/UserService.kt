package dev.andresoares.dev.andresoares.service

import dev.andresoares.dev.andresoares.dto.UserCreateRequest
import dev.andresoares.dev.andresoares.dto.UserResponse
import dev.andresoares.dev.andresoares.dto.UserUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Interface que define o contrato para operações de serviço relacionadas a Users.
 *
 * Esta interface promove:
 * - Desacoplamento entre camadas
 * - Facilidade para testes (mocks/stubs)
 * - Inversão de dependência (SOLID)
 * - Possibilidade de múltiplas implementações
 */
interface UserService {

    /**
     * Retorna todos os usuários cadastrados.
     * @return Lista de todos os usuários em formato DTO
     */
    fun getAllUsers(): List<UserResponse>

    /**
     * Retorna todos os usuários com paginação e ordenação.
     * @param pageable Configurações de paginação e ordenação
     * @return Página de usuários em formato DTO
     */
    fun getAllUsers(pageable: Pageable): Page<UserResponse>

    // ...existing code...

    /**
     * Busca um usuário específico por ID.
     * @param id Identificador do usuário
     * @return Usuário encontrado em formato DTO
     * @throws ResourceNotFoundException se usuário não for encontrado
     */
    fun getUserById(id: Long): UserResponse

    /**
     * Busca usuários por nome (case-insensitive).
     * @param name Texto para busca no nome do usuário
     * @return Lista de usuários que contêm o nome buscado
     */
    fun searchUsersByName(name: String): List<UserResponse>

    /**
     * Cria um usuário.
     * @param request Dados para criação do usuário
     * @return Usuário criado em formato DTO
     */
    fun createUser(request: UserCreateRequest): UserResponse

    /**
     * Atualiza usuário existente.
     * @param id Identificador do usuário a ser atualizado
     * @param request Dados para atualização do usuário
     * @return Usuário atualizado em formato DTO
     * @throws ResourceNotFoundException se usuário não for encontrado
     */
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse

    /**
     * Deleta usuário por ID.
     * @param id Identificador do usuário a ser deletado
     * @throws ResourceNotFoundException se usuário não for encontrado
     */
    fun deleteUser(id: Long)
}
