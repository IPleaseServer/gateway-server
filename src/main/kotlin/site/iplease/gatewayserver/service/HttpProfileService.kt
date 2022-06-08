package site.iplease.gatewayserver.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import site.iplease.gatewayserver.data.response.ProfileExistsResponse
import site.iplease.gatewayserver.data.response.ProfileResponse

@Service
class HttpProfileService(
    private val webClientBuilder: WebClient.Builder
): ProfileService {
    override fun getProfileByAccessToken(accessToken: String): Mono<ProfileResponse> =
        webClientBuilder.baseUrl("lb://account-server")
            .build()
            .get()
            .uri("/access-token/$accessToken")
            .retrieve()
            .bodyToMono(ProfileResponse::class.java)

    override fun existProfileByAccessToken(accessToken: String): Mono<Boolean> =
        webClientBuilder.baseUrl("lb://account-server")
            .build()
            .get()
            .uri("/access-token/$accessToken/exists")
            .retrieve()
            .bodyToMono(ProfileExistsResponse::class.java)
            .map { it.isExists }

}