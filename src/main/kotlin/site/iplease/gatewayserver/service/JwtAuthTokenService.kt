package site.iplease.gatewayserver.service

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import site.iplease.gatewayserver.data.dto.AccountDto
import site.iplease.gatewayserver.data.response.ProfileResponse

@Service
class JwtAuthTokenService(
    private val profileService: ProfileService
): AuthTokenService {
    override fun containToken(headers: HttpHeaders) = headers.containsKey(HttpHeaders.AUTHORIZATION)
        .let { if(it) headers[HttpHeaders.AUTHORIZATION]!![0].contains("Bearer ") else false }
    override fun extractToken(headers: HttpHeaders) = headers.getOrEmpty(HttpHeaders.AUTHORIZATION)[0].substring("Bearer ".length)
    override fun validToken(token: String) = profileService.existProfileByAccessToken(token)
    override fun decodeToken(token: String): AccountDto = profileService.getProfileByAccessToken(accessToken = token).toAccountDto()
}

private fun ProfileResponse.toAccountDto(): AccountDto = AccountDto(accountId = this.common.accountId, permission = this.common.permission)
