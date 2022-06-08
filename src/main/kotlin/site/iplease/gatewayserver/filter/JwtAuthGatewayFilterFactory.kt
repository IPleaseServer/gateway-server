package site.iplease.gatewayserver.filter

import org.slf4j.LoggerFactory
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
import site.iplease.gatewayserver.data.type.AuthorizeStatusType
import site.iplease.gatewayserver.exception.EmptyTokenException
import site.iplease.gatewayserver.exception.WrongTokenException
import site.iplease.gatewayserver.service.AuthTokenService

@Component
class JwtAuthGatewayFilterFactory(
    private val service: AuthTokenService
): AbstractGatewayFilterFactory<FilterConfig>(FilterConfig::class.java) {
    val logger = LoggerFactory.getLogger(this::class.java)

    override fun apply(config: FilterConfig): GatewayFilter =
        GatewayFilter { exchange, chain ->
            fun error(message: String, status: HttpStatus): Mono<Void> = error(exchange.response, message, status)
            fun error(message: String): Mono<Void> = error(message = message, status = HttpStatus.BAD_REQUEST)

            return@GatewayFilter Mono.just(exchange)
                .map { it.request.headers }//요청의 헤더를 추출한다.
                .flatMap {  headers ->
                    service.containToken(headers).flatMap { containToken -> //헤더에 토큰이 포함되어있는지 검사한다.
                        if(containToken) service.extractToken(headers) //토큰이 포함되어있으면 토큰을 추출한다.
                        else Mono.error(EmptyTokenException()) //토큰이 포함되어있지 않으면 오류를 반환한다.
                    }
                }.flatMap { token ->
                    service.validToken(token).flatMap { validToken -> //토큰을 검증한다.
                        if(validToken) service.decodeToken(token) //검증에 성공하면 토큰을 해석한다.
                        else Mono.error(WrongTokenException()) //검증에 실패하면 오류를 반환한다.
                    }
                }.map { account -> addAuthAccount(exchange.request, account).let { AuthorizeStatusType.SUCCESS } } //요청정보에 인증된 계정정보를 추가한다.
                .onErrorReturn(EmptyTokenException::class.java, AuthorizeStatusType.EMPTY_TOKEN)
                .onErrorReturn(WrongTokenException::class.java, AuthorizeStatusType.WRONG_TOKEN)
                .onErrorReturn(AuthorizeStatusType.UNKNOWN_ERROR)
                .flatMap { status ->
                    when(status!!) {
                        AuthorizeStatusType.SUCCESS -> chain.filter(exchange) //모든 작업이 성공하면 다음 filter에게로 chaining한다.
                        //오류 발생시, 이에 해당하는 반환값을 구성한다.
                        AuthorizeStatusType.EMPTY_TOKEN -> error("요청에 access token이 포함되어있지 않습니다.")
                        AuthorizeStatusType.WRONG_TOKEN -> error("access token검증에 실패하였습니다.")
                        //만약 예측하지 못한 오류 발생시, 해당 오류를 로깅하고 반환값을 구성한다.
                        AuthorizeStatusType.UNKNOWN_ERROR -> {
                            logger.warn("인증필터 로직을 수행하는 도중, 알 수 없는 오류가 발생했습니다!")
                            error("예기치 못한 오류가 발생하였습니다.")
                        }
                    }
                }
        }

    private fun addAuthAccount(request: ServerHttpRequest, account: AccountDto) {
        request.mutate()
            .header("X-Authorization-Id", account.accountId.toString())
            .header("X-Authorization-Permission", account.permission.toString())
            .build()
    }

    private fun error(response: ServerHttpResponse, message: String, status: HttpStatus): Mono<Void> =
        response.apply { statusCode = status }
            .let { response to response.bufferFactory().wrap(message.toByteArray()) }
            .let { it.first.writeWith(it.second.toMono()) }
}