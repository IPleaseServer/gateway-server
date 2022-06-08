package site.iplease.gatewayserver.service

import reactor.core.publisher.Mono
import site.iplease.gatewayserver.data.response.ProfileResponse

interface ProfileService {
    fun getProfileByAccessToken(accessToken: String): Mono<ProfileResponse>
    fun existProfileByAccessToken(accessToken: String): Mono<Boolean>
}
