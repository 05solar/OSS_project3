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
                                CAT TABLE 음식점 관리 시스템 API 문서입니다.

                                **인증 방법**: 로그인(`POST /api/auth/login`) 후 발급받은 `accessToken`을 \
                                우측 상단 **Authorize** 버튼에 입력하세요.

                                **권한 구분**:
                                - 인증 없음: 메뉴 조회, 리뷰 조회, AI 기능 일부
                                - USER 권한: 주문 생성, 내 주문 조회, 리뷰 작성
                                - ADMIN 권한: 관리자 전용 엔드포인트 (`/api/admin/...`)
                                """)
                        .contact(new Contact().name("CAT TABLE").email("admin@cattable.kr")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("로그인 후 발급받은 accessToken을 입력하세요 (Bearer 접두사 불필요)")));
    }
}
