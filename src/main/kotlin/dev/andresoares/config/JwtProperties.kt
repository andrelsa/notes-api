package dev.andresoares.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "",
    var accessTokenExpiration: Long = 3600000, // 1 hora em ms
    var refreshTokenExpiration: Long = 604800000 // 7 dias em ms
)
