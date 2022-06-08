package site.iplease.gatewayserver.service

import org.springframework.http.HttpHeaders
import site.iplease.gatewayserver.data.dto.AccountDto

interface AuthTokenService {
    fun extractToken(headers: HttpHeaders): String
    fun validToken(token: String): Boolean
    fun decodeToken(token: String): AccountDto
    fun containToken(headers: HttpHeaders): Boolean
}
