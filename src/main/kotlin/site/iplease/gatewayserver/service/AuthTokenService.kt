package site.iplease.gatewayserver.service

import org.springframework.http.HttpHeaders
import reactor.core.publisher.Mono
import site.iplease.gatewayserver.data.dto.AccountDto
import site.iplease.gatewayserver.data.type.PermissionType

interface AuthTokenService {
    fun extractToken(headers: HttpHeaders): Mono<String>
    fun validToken(token: String): Mono<Boolean>
    fun decodeToken(token: String): Mono<AccountDto>
    fun containToken(headers: HttpHeaders): Mono<Boolean>
    fun validPermission(permissions: List<PermissionType>, permission: PermissionType): Mono<Boolean>
}
