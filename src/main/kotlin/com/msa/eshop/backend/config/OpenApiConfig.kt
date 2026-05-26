package com.msa.eshop.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        val schemeName = "bearerAuth"
        return OpenAPI()
            .info(
                Info()
                    .title("Shop Market Compose API")
                    .version("1.0.0")
                    .description("Android-compatible e-commerce API built with Spring Boot and Kotlin")
            )
            .components(
                Components().addSecuritySchemes(
                    schemeName,
                    SecurityScheme()
                        .name(schemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(schemeName))
    }
}
