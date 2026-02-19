package dev.andresoares.service.impl

import dev.andresoares.dto.*
import dev.andresoares.exception.ResourceNotFoundException
import dev.andresoares.model.RefreshToken
import dev.andresoares.repository.RefreshTokenRepository
import dev.andresoares.repository.UserRepository
import dev.andresoares.security.JwtTokenProvider
import dev.andresoares.service.AuthService
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) : AuthService {

    @Transactional
    override fun login(request: LoginRequest): LoginResponse {
        // Buscar usuário pelo email
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BadCredentialsException("Invalid email or password") }

        // Verificar senha
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid email or password")
        }

        // Gerar tokens
        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
        val refreshTokenString = jwtTokenProvider.generateRefreshToken(user.id)

        // Salvar refresh token no banco
        val refreshToken = RefreshToken(
            token = refreshTokenString,
            user = user,
            expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
        )
        refreshTokenRepository.save(refreshToken)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenString,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000, // converter para segundos
            user = UserInfo(
                id = user.id,
                name = user.name,
                email = user.email,
                roles = user.roles
            )
        )
    }

    @Transactional
    override fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse {
        // Validar o refresh token JWT
        if (!jwtTokenProvider.validateToken(request.refreshToken) ||
            !jwtTokenProvider.isRefreshToken(request.refreshToken)) {
            throw BadCredentialsException("Invalid refresh token")
        }

        // Buscar refresh token no banco
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { ResourceNotFoundException("Refresh token not found") }

        // Verificar se está revogado
        if (refreshToken.revoked) {
            throw BadCredentialsException("Refresh token has been revoked")
        }

        // Verificar se está expirado
        if (refreshToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw BadCredentialsException("Refresh token has expired")
        }

        val user = refreshToken.user

        // Gerar novos tokens
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
        val newRefreshTokenString = jwtTokenProvider.generateRefreshToken(user.id)

        // Revogar o refresh token antigo
        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        // Salvar novo refresh token
        val newRefreshToken = RefreshToken(
            token = newRefreshTokenString,
            user = user,
            expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
        )
        refreshTokenRepository.save(newRefreshToken)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshTokenString,
            expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000
        )
    }

    @Transactional
    override fun logout(request: LogoutRequest): MessageResponse {
        // Buscar e revogar o refresh token
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { ResourceNotFoundException("Refresh token not found") }

        refreshToken.revoked = true
        refreshTokenRepository.save(refreshToken)

        return MessageResponse("Logout successful")
    }
}
