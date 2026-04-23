package com.example.gateway;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAT TABLE API")
                        .version("1.0.0")
                        .description("""
                                CAT TABLE gateway API documentation.

                                Authentication:
                                1. Call `POST /api/auth/login`
                                2. Copy the returned `accessToken`
                                3. Paste it into the `Authorize` dialog

                                Roles:
                                - Public: browse menus, reviews, and AI helper endpoints
                                - USER: place orders, view own orders, create reviews
                                - ADMIN: access `/api/admin/*` endpoints
                                """)
                        .contact(new Contact().name("CAT TABLE").email("admin@cattable.kr")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste the access token issued by the login API. The Bearer prefix is optional.")));
    }
}
