package site.iplease.gatewayserver.service

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import site.iplease.gatewayserver.data.dto.AccountDto
import site.iplease.gatewayserver.data.response.ProfileResponse
import site.iplease.gatewayserver.data.type.PermissionType

@Service
class JwtAuthTokenService(
    private val profileService: ProfileService
): AuthTokenService {
    override fun containToken(headers: HttpHeaders) =
        Unit.toMono()
            .map { headers.containsKey(HttpHeaders.AUTHORIZATION) }
            .map { containAuthHeader -> if(containAuthHeader) headers[HttpHeaders.AUTHORIZATION]!![0].contains("Bearer ") else false }

    override fun validPermission(permissions: List<PermissionType>, permission: PermissionType): Mono<Boolean> =
        Unit.toMono().map { permissions.contains(permission) }

    override fun extractToken(headers: HttpHeaders) =
        Unit.toMono()
            .map { headers.getOrEmpty(HttpHeaders.AUTHORIZATION)[0] }
            .map { it.substring("Bearer ".length) }
    override fun validToken(token: String) =
        profileService.existProfileByAccessToken(token)
    override fun decodeToken(token: String) =
        profileService.getProfileByAccessToken(accessToken = token)
            .map { it.toAccountDto() }
}

private fun ProfileResponse.toAccountDto(): AccountDto = AccountDto(accountId = this.common.accountId, permission = this.common.permission)
