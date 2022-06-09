package site.iplease.gatewayserver.config
//
//import com.linecorp.armeria.client.grpc.GrpcClients
//import org.springframework.cloud.client.discovery.DiscoveryClient
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import site.iplease.accountserver.lib.ReactorProfileServiceGrpc.ReactorProfileServiceStub
//
//@Configuration
//class GrpcConfig(
//    val discoveryClient: DiscoveryClient
//) {
//    @Bean
//    fun reactorProfileServiceStub(): ReactorProfileServiceStub =
//            discoveryClient.getInstances("account-server")
//            .let { if (it.isEmpty()) throw RuntimeException("account-server not found") else it }
//            .let { it.random().uri }
//            .let { GrpcClients.newClient(it, ReactorProfileServiceStub::class.java) }
//}