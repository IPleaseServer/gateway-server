package site.iplease.gatewayserver.config

import org.springdoc.core.SwaggerUiConfigParameters
import org.springframework.boot.CommandLineRunner
import org.springframework.cloud.gateway.route.RouteDefinition
import org.springframework.cloud.gateway.route.RouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openApiGroups(
        locator: RouteDefinitionLocator,
        parameters: SwaggerUiConfigParameters
    ): CommandLineRunner = CommandLineRunner { args ->
        locator.routeDefinitions.toStream()
            .map(RouteDefinition::getId)
            .filter { id -> id.matches(Regex(".*-doc")) }
            .map { id -> id.substring(0, id.length - 4) }
            .map { domain -> "$domain-api" }
            .forEach(parameters::addGroup)
    }
}