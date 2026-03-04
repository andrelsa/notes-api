package dev.andresoares.security

import dev.andresoares.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    /**
     * Gera um token de acesso (Access Token)
     */
    fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.accessTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Gera um token de atualização (Refresh Token)
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshTokenExpiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .id(UUID.randomUUID().toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * Extrai o ID do usuário do token
     */
    fun getUserIdFromToken(token: String): Long {
        return getAllClaimsFromToken(token).subject.toLong()
    }

    /**
     * Extrai o email do token
     */
    fun getEmailFromToken(token: String): String? {
        return getAllClaimsFromToken(token)["email"] as? String
    }

    /**
     * Valida o token
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida se o token pertence ao usuário
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val userId = getUserIdFromToken(token)
            val email = getEmailFromToken(token)
            email == userDetails.username && !isTokenExpired(getAllClaimsFromToken(token))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica se é um Access Token
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            claims["type"] == "access"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica se é um Refresh Token
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getAllClaimsFromToken(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtém a data de expiração do token
     */
    fun getExpirationDateFromToken(token: String): Date {
        return getAllClaimsFromToken(token).expiration
    }

    /**
     * Extrai todas as claims do token
     */
    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * Verifica se o token está expirado
     */
    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }

    /**
     * Obtém o tempo de expiração do access token em milissegundos
     */
    fun getAccessTokenExpiration(): Long {
        return jwtProperties.accessTokenExpiration
    }

    /**
     * Obtém o tempo de expiração do refresh token em milissegundos
     */
    fun getRefreshTokenExpiration(): Long {
        return jwtProperties.refreshTokenExpiration
    }
}
