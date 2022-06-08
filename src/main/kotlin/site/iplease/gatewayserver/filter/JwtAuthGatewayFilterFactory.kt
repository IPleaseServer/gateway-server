package site.iplease.gatewayserver.filter

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import site.iplease.gatewayserver.data.dto.AccountDto
import site.iplease.gatewayserver.config.FilterConfig
import site.iplease.gatewayserver.service.AuthTokenService

@Component
class JwtAuthGatewayFilterFactory(
    private val service: AuthTokenService
): AbstractGatewayFilterFactory<FilterConfig>(FilterConfig::class.java) {

    override fun apply(config: FilterConfig): GatewayFilter =
        GatewayFilter { exchange, chain ->
            fun error(message: String, status: HttpStatus): Mono<Void> = error(exchange.response, message, status)
            fun error(message: String): Mono<Void> = error(message = message, status = HttpStatus.BAD_REQUEST)
            println(exchange.request.path)
            println(exchange.request.uri)
            exchange.request.headers.forEach{println(it.key + " " + it.value)}

            exchange.request.headers //요청의 헤더를 추출한다.
                .let { headers -> //헤더에 토큰이 포함되어있는지 검사한다.
                    if (service.containToken(headers)) service.extractToken(headers) //토큰이 포함되어있으면 토큰을 추출한다.
                    else return@GatewayFilter error("요청에 access token이 포함되어있지 않습니다.")//토큰이 포함되어있지 않으면 오류를 반환한다.
                }.let { token -> //토큰을 검증한다.
                    if(service.validToken(token)) service.decodeToken(token)//검증에 성공하면 토큰을 해석한다.
                    else return@GatewayFilter error("access token검증에 실패하였습니다.")//토큰검증에 실패하면 오류를 반환한다.
                }.let { account -> addAuthAccount(exchange.request, account) }//요청정보에 인증된 계정정보를 추가한다.
            return@GatewayFilter chain.filter(exchange)
        }

    private fun addAuthAccount(request: ServerHttpRequest, account: AccountDto) {
        request.mutate()
            .header("X-Authorization-Id", account.accountId.toString())
            .header("X-Authorization-Permission", account.permission)
            .build()
    }

    private fun error(response: ServerHttpResponse, message: String, status: HttpStatus): Mono<Void> =
        response.apply { statusCode = status }
            .let { response to response.bufferFactory().wrap(message.toByteArray()) }
            .let { it.first.writeWith(it.second.toMono()) }
}