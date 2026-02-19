package dev.andresoares.service

import dev.andresoares.dto.*

interface AuthService {
    fun login(request: LoginRequest): LoginResponse
    fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse
    fun logout(request: LogoutRequest): MessageResponse
}
