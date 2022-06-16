package site.iplease.gatewayserver.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import site.iplease.gatewayserver.data.dto.AccountDto
import site.iplease.gatewayserver.data.dto.FilterConfigDto
import site.iplease.gatewayserver.data.type.AuthorizeStatusType
import site.iplease.gatewayserver.data.type.PermissionType
import site.iplease.gatewayserver.exception.EmptyTokenException
import site.iplease.gatewayserver.exception.PermissionDeniedException
import site.iplease.gatewayserver.exception.WrongTokenException
import site.iplease.gatewayserver.service.AuthTokenService

@Component
class JwtAuthFilter(
    private val service: AuthTokenService
): AbstractGatewayFilterFactory<FilterConfigDto>(FilterConfigDto::class.java) {
    val logger = LoggerFactory.getLogger(this::class.java)

    override fun apply(config: FilterConfigDto): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val permissions = getPermissions(config.permissions) //Config에서 해당 path에 접근이 허용된 권한들을 추출한다.
            return@GatewayFilter Mono.just(exchange)
                .map { it.request.headers }//요청의 헤더를 추출한다.
                .flatMap { headers -> validate(headers = headers, permissions = permissions) }//요청에 대한 검증로직을 수행한다.
                .map { account -> process(account = account, request = exchange.request) } //요청에 대한 필터링 로직을 수행한다.
                .let { mono -> handleError(mono) } //mono내에서 발생한 오류를 핸들링한다.
                .map { status -> checkGuestPolicy(status = status, permissions = permissions) } //GUEST에 대한 예외정책을 검사한다.
                .flatMap { status -> complete(status = status, chain = chain, exchange = exchange) } //인증상태에 따라, 필터링 로직을 마무리한다.
        } }

    private fun validate(permissions: List<PermissionType>, headers: HttpHeaders) =
        checkHeaders(headers) //헤더를 검사하여 토큰을 추출한다.
            .flatMap { token -> checkToken(token)} //토큰을 검사하여 계정을 추출한다.
            .flatMap { account -> checkAccount(permissions, account) } //계정과 권한을 검사한다.

    private fun process(request: ServerHttpRequest, account: AccountDto) =
        addAuthAccount(request, account).let { AuthorizeStatusType.SUCCESS } //요청정보에 인증된 계정정보를 추가한다.

    //발생한 오류에 따라, 인증상태를 갱신하거나 로깅을 수행한다.
    private fun handleError(mono: Mono<AuthorizeStatusType>) =
        mono.onErrorReturn(EmptyTokenException::class.java, AuthorizeStatusType.EMPTY_TOKEN)//토큰이 비어있어서 오류가 발생할 경우 상태를 EMPTY_TOKEN으로 갱신한다.
            .onErrorReturn(WrongTokenException::class.java, AuthorizeStatusType.WRONG_TOKEN) //잘못된 토큰으로인해 오류가 발생할 경우 상태를 WRONG_TOKEN으로 갱신한다.
            .onErrorReturn(PermissionDeniedException::class.java, AuthorizeStatusType.PERMISSION_DENIED) //권한이 없어 오류 발생할 경우 상태를 PERMISSION_DENIED으로 갱신한다.
            .doOnError{//만약 예측하지 못한 오류가 발생할 경우
                //해당 오류를 로깅한다.
                logger.warn("인증필터 로직을 수행하는 도중, 알 수 없는 오류가 발생했습니다!")
                logger.warn(it.localizedMessage)
            }.onErrorReturn(AuthorizeStatusType.UNKNOWN_ERROR) //상태를 UNKNOWN_ERROR으로 갱신한다.

    //만약 GUEST의 접근이 허용된다면, 토큰인증여부에 관계없이 검증을 통과시킨다. (단, 인증에 성공하여, 계정정보를 받아왔을 경우 제외)
    private fun checkGuestPolicy(status: AuthorizeStatusType, permissions: List<PermissionType>)  =
        if(permissions.contains(PermissionType.GUEST) && status != AuthorizeStatusType.PERMISSION_DENIED) AuthorizeStatusType.SUCCESS else status

    //인증상태에 따라, error메세지 반환 또는 필터 체이닝등의 작업을 수행한다.
    private fun complete(status: AuthorizeStatusType, exchange: ServerWebExchange, chain: GatewayFilterChain) =
        when(status) {
            AuthorizeStatusType.SUCCESS -> chain.filter(exchange) //모든 작업이 성공하면 다음 filter에게로 chaining한다.
            //오류 발생시, 이에 해당하는 반환값을 구성한다.
            AuthorizeStatusType.EMPTY_TOKEN -> error(response = exchange.response, status = HttpStatus.BAD_REQUEST, message = "요청에 access token이 포함되어있지 않습니다.")
            AuthorizeStatusType.WRONG_TOKEN -> error(response = exchange.response, status = HttpStatus.BAD_REQUEST, message = "access token검증에 실패하였습니다.")
            AuthorizeStatusType.UNKNOWN_ERROR ->  error(response = exchange.response, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "예기치 못한 오류가 발생하였습니다.")
            AuthorizeStatusType.PERMISSION_DENIED ->  error(response = exchange.response, status = HttpStatus.FORBIDDEN, message = "해당 기능에 접근할 권한이 없습니다.")
        }

    private fun getPermissions(permissions: List<String>): List<PermissionType> =
        permissions.map{ permission ->
            //만약 와일드카드가 포함되어있을시, 모든권한을 담은 리스트를 반환한다.
            if(permission == "*") return PermissionType.values().toList()
            else PermissionType.valueOf(permission)
        }

    private fun checkAccount(permissions: List<PermissionType>, account: AccountDto) =
        service.validPermission(permissions, account.permission)
            .flatMap {
                if(it) account.toMono()
                else Mono.error(PermissionDeniedException())
            }

    private fun checkToken(token: String) =
        service.validToken(token).flatMap { validToken -> //토큰을 검증한다.
            if(validToken) service.decodeToken(token) //검증에 성공하면 토큰을 해석한다.
            else Mono.error(WrongTokenException()) //검증에 실패하면 오류를 반환한다.
        }

    private fun checkHeaders(headers: HttpHeaders) =
        service.containToken(headers).flatMap { containToken -> //헤더에 토큰이 포함되어있는지 검사한다.
            if (containToken) service.extractToken(headers) //토큰이 포함되어있으면 토큰을 추출한다.
            else Mono.error(EmptyTokenException()) //토큰이 포함되어있지 않으면 오류를 반환한다.
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