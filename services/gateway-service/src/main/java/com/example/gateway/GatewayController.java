package com.example.gateway;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class GatewayController {
    private final RestClient restClient;
    private final JwtSupport jwtSupport;
    private final String authBaseUrl;
    private final String menuBaseUrl;
    private final String orderBaseUrl;
    private final String reviewBaseUrl;
    private final String aiRecommendationBaseUrl;
    private final String aiReviewWriterBaseUrl;
    private final String aiOperationsBaseUrl;

    public GatewayController(
            RestClient restClient,
            JwtSupport jwtSupport,
            @Value("${app.auth-base-url}") String authBaseUrl,
            @Value("${app.menu-base-url}") String menuBaseUrl,
            @Value("${app.order-base-url}") String orderBaseUrl,
            @Value("${app.review-base-url}") String reviewBaseUrl,
            @Value("${app.ai-recommendation-base-url}") String aiRecommendationBaseUrl,
            @Value("${app.ai-review-writer-base-url}") String aiReviewWriterBaseUrl,
            @Value("${app.ai-operations-base-url}") String aiOperationsBaseUrl) {
        this.restClient = restClient;
        this.jwtSupport = jwtSupport;
        this.authBaseUrl = authBaseUrl;
        this.menuBaseUrl = menuBaseUrl;
        this.orderBaseUrl = orderBaseUrl;
        this.reviewBaseUrl = reviewBaseUrl;
        this.aiRecommendationBaseUrl = aiRecommendationBaseUrl;
        this.aiReviewWriterBaseUrl = aiReviewWriterBaseUrl;
        this.aiOperationsBaseUrl = aiOperationsBaseUrl;
    }

    // ───────────────────────────────────────────────
    //  AUTH
    // ───────────────────────────────────────────────

    @Tag(name = "인증 (Auth)", description = "회원가입, 로그인, 토큰 갱신, 내 정보 조회")
    @Operation(
        summary = "회원가입",
        description = "새 사용자 계정을 생성합니다. username, password, role(USER/ADMIN) 을 전달하면 계정이 생성되고 JWT 토큰을 반환합니다.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"username\":\"user1\",\"password\":\"pass1234\",\"role\":\"USER\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공 — accessToken, refreshToken 반환",
                    content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 username")
        }
    )
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Object register(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        return post(authBaseUrl + "/auth/register", body);
    }

    @Tag(name = "인증 (Auth)", description = "회원가입, 로그인, 토큰 갱신, 내 정보 조회")
    @Operation(
        summary = "로그인",
        description = "username 과 password 로 로그인합니다. 성공 시 accessToken(30분)과 refreshToken(7일)을 반환합니다.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"username\":\"admin\",\"password\":\"admin1234\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 — accessToken, refreshToken 반환",
                    content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
            @ApiResponse(responseCode = "401", description = "잘못된 아이디/비밀번호")
        }
    )
    @PostMapping("/auth/login")
    public Object login(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        return post(authBaseUrl + "/auth/login", body);
    }

    @Tag(name = "인증 (Auth)", description = "회원가입, 로그인, 토큰 갱신, 내 정보 조회")
    @Operation(
        summary = "토큰 갱신",
        description = "만료된 accessToken 을 refreshToken 으로 갱신합니다.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"refreshToken\":\"<refresh_token>\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "새 accessToken 반환",
                    content = @Content(schema = @Schema(implementation = AuthTokensResponse.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 refreshToken")
        }
    )
    @PostMapping("/auth/refresh")
    public Object refresh(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        return post(authBaseUrl + "/auth/refresh", body);
    }

    @Tag(name = "인증 (Auth)", description = "회원가입, 로그인, 토큰 갱신, 내 정보 조회")
    @Operation(
        summary = "내 정보 조회",
        description = "JWT 토큰으로 현재 로그인된 사용자의 정보(userId, username, role)를 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 반환",
                    content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰 없음 또는 만료")
        }
    )
    @GetMapping("/auth/me")
    public Object me(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        return restClient.get().uri(authBaseUrl + "/auth/me").header("Authorization", authorization).retrieve().body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  CUSTOMER — 메뉴
    // ───────────────────────────────────────────────

    @Tag(name = "고객용 — 메뉴 (Customer Menu)", description = "메뉴 목록 조회, 카테고리 필터")
    @Operation(
        summary = "메뉴 목록 조회",
        description = "판매 중인 메뉴 목록을 반환합니다. category 또는 keyword 로 필터링할 수 있습니다.",
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "메뉴 배열 반환. 각 항목: id, name, description, category, price, imageUrl, etaMinutes, available",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuItemResponse.class))))
        }
    )
    @GetMapping("/customer/menus")
    public Object menus(
        @Parameter(description = "카테고리 필터 (예: 한식, 중식, 일식)") @RequestParam(required = false) String category,
        @Parameter(description = "키워드 검색 (메뉴 이름/설명)") @RequestParam(required = false) String keyword
    ) {
        String url = menuBaseUrl + "/menus";
        if (category != null || keyword != null) {
            StringBuilder builder = new StringBuilder(url).append("?");
            if (category != null) {
                builder.append("category=").append(category).append("&");
            }
            if (keyword != null) {
                builder.append("keyword=").append(keyword);
            }
            url = builder.toString();
        }
        return restClient.get().uri(url).retrieve().body(Object.class);
    }

    @Tag(name = "고객용 — 메뉴 (Customer Menu)", description = "메뉴 목록 조회, 카테고리 필터")
    @Operation(
        summary = "카테고리 목록 조회",
        description = "등록된 메뉴의 카테고리 목록(중복 제거)을 반환합니다.",
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 문자열 배열 (예: [\"한식\", \"중식\", \"일식\"])",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
        }
    )
    @GetMapping("/customer/categories")
    public Object categories() {
        return restClient.get().uri(menuBaseUrl + "/menus/categories").retrieve().body(Object.class);
    }

    @Tag(name = "관리자 — 메뉴 관리 (Admin Menu)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "카테고리 목록 조회 (관리자)",
        description = "관리자 화면에서 사용할 카테고리 목록을 조회합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @GetMapping("/admin/categories")
    public Object adminCategories(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        requireAdmin(authorization);
        return restClient.get().uri(menuBaseUrl + "/menus/categories").retrieve().body(Object.class);
    }

    @Tag(name = "관리자 — 메뉴 관리 (Admin Menu)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "카테고리 추가",
        description = "새 카테고리를 등록합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"name\":\"브런치\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "카테고리 등록 성공",
                    content = @Content(schema = @Schema(implementation = CategoryCreateResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createCategory(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        requireAdmin(authorization);
        return post(menuBaseUrl + "/menus/categories", body);
    }

    // ───────────────────────────────────────────────
    //  CUSTOMER — 리뷰
    // ───────────────────────────────────────────────

    @Tag(name = "고객용 — 리뷰 (Customer Review)", description = "리뷰 조회 및 작성")
    @Operation(
        summary = "전체 리뷰 목록 조회",
        description = "등록된 모든 리뷰를 반환합니다. 별점, 작성자, 메뉴명, 내용 포함.",
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리뷰 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewResponseDoc.class))))
        }
    )
    @GetMapping("/customer/reviews")
    public Object reviews() {
        return restClient.get().uri(reviewBaseUrl + "/reviews").retrieve().body(Object.class);
    }

    @Tag(name = "고객용 — 리뷰 (Customer Review)", description = "리뷰 목록 조회와 리뷰 작성")
    @Operation(
        summary = "리뷰 작성",
        description = "로그인한 사용자가 메뉴 리뷰를 작성합니다. 실제 요청 바디는 menuName, content, rating, source 필드를 사용합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"menuName\":\"김치찌개\",\"content\":\"정말 맛있어요!\",\"rating\":5,\"source\":\"customer-web\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
        }
    )
    @PostMapping("/customer/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createReview(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        JwtSupport.UserContext user = requireUser(authorization);
        return restClient.post().uri(reviewBaseUrl + "/reviews")
                .header("X-User-Id", String.valueOf(user.userId()))
                .header("X-Username", user.username())
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  CUSTOMER — 주문
    // ───────────────────────────────────────────────

    @Tag(name = "고객용 — 주문 (Customer Order)", description = "주문 생성과 내 주문 조회")
    @Operation(
        summary = "주문 생성",
        description = "로그인한 사용자가 메뉴를 주문합니다. 각 item은 menuId, menuName, quantity, unitPrice, etaMinutes 를 포함해야 합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"items\":[{\"menuId\":1,\"menuName\":\"김치찌개\",\"quantity\":2,\"unitPrice\":9000,\"etaMinutes\":18}],\"totalAmount\":18000,\"notes\":\"덜 맵게 해주세요\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "주문 생성 성공 — orderId 포함 주문 정보 반환",
                    content = @Content(schema = @Schema(implementation = OrderCreateResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
        }
    )
    @PostMapping("/customer/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createOrder(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        JwtSupport.UserContext user = requireUser(authorization);
        return restClient.post().uri(orderBaseUrl + "/orders")
                .header("X-User-Id", String.valueOf(user.userId()))
                .header("X-Username", user.username())
                .header("X-User-Role", user.role())
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    @Tag(name = "고객용 — 주문 (Customer Order)", description = "주문 생성 및 내 주문 조회")
    @Operation(
        summary = "내 주문 목록 조회",
        description = "로그인한 사용자 본인의 주문 내역을 반환합니다. 주문 상태(RECEIVED/COOKING/READY/DELIVERED) 포함.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponseDoc.class)))),
            @ApiResponse(responseCode = "401", description = "로그인 필요")
        }
    )
    @GetMapping("/customer/orders")
    public Object myOrders(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        JwtSupport.UserContext user = requireUser(authorization);
        return restClient.get().uri(orderBaseUrl + "/orders")
                .header("X-User-Id", String.valueOf(user.userId()))
                .header("X-Username", user.username())
                .header("X-User-Role", user.role())
                .retrieve()
                .body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  CUSTOMER — AI
    // ───────────────────────────────────────────────

    @Tag(name = "고객용 — AI 기능 (Customer AI)", description = "AI 리뷰 초안 작성, 메뉴 추천, 혼잡도 분석")
    @Operation(
        summary = "AI 리뷰 초안 작성",
        description = "메뉴명, 선택 키워드, 별점을 전달하면 AI가 리뷰 초안을 작성합니다.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"menuName\":\"김치찌개\",\"keywords\":[\"칼칼함\",\"푸짐함\"],\"rating\":5}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI가 생성한 리뷰 초안 텍스트 반환",
                    content = @Content(schema = @Schema(implementation = ReviewDraftResponse.class)))
        }
    )
    @PostMapping("/customer/review-draft")
    public Object reviewDraft(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        return post(aiReviewWriterBaseUrl + "/write-review", body);
    }

    @Tag(name = "고객용 — AI 기능 (Customer AI)", description = "AI 리뷰 초안 작성, 메뉴 추천, 혼잡도 분석")
    @Operation(
        summary = "AI 메뉴 추천",
        description = "사용자 메시지와 후보 메뉴 목록을 전달하면 AI가 메뉴를 추천합니다.",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\":\"든든하고 국물 있는 메뉴 추천해줘\",\"menus\":[{\"id\":1,\"name\":\"김치찌개\",\"description\":\"칼칼한 찌개\",\"keywords\":\"국물,매콤\",\"price\":9000,\"etaMinutes\":15,\"available\":true},{\"id\":2,\"name\":\"비빔밥\",\"description\":\"채소와 고기 비빔밥\",\"keywords\":\"든든함,건강\",\"price\":10000,\"etaMinutes\":12,\"available\":true}]}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 추천 메뉴 및 이유 반환",
                    content = @Content(schema = @Schema(implementation = RecommendMenuResponse.class)))
        }
    )
    @PostMapping("/customer/recommend-menu")
    public Object recommendMenu(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        return post(aiRecommendationBaseUrl + "/recommend-menu", body);
    }

    @Tag(name = "고객용 — AI 기능 (Customer AI)", description = "AI 리뷰 초안 작성, 메뉴 추천, 혼잡도 분석")
    @Operation(
        summary = "혼잡도 AI 분석",
        description = "현재 주문 데이터를 기반으로 AI가 가게 혼잡도와 대기시간을 분석합니다.",
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "혼잡도 분석 결과 텍스트 반환",
                    content = @Content(schema = @Schema(implementation = TrafficResponse.class)))
        }
    )
    @GetMapping("/customer/traffic")
    public Object traffic() {
        Object sales = restClient.get().uri(orderBaseUrl + "/orders/summary").retrieve().body(Object.class);
        return post(aiOperationsBaseUrl + "/analyze-traffic", Map.of("salesSummary", sales));
    }

    // ───────────────────────────────────────────────
    //  ADMIN — 대시보드 / 통계
    // ───────────────────────────────────────────────

    @Tag(name = "관리자 — 대시보드 (Admin Dashboard)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "대시보드 통계 조회",
        description = "매출 요약, 리뷰 요약, 전체 메뉴 현황을 한 번에 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "{ sales: {...}, reviews: {...}, menus: [...] }",
                    content = @Content(schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @GetMapping("/admin/dashboard")
    public Object dashboard(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        requireAdmin(authorization);
        Object sales = restClient.get().uri(orderBaseUrl + "/orders/summary").retrieve().body(Object.class);
        Object reviews = restClient.get().uri(reviewBaseUrl + "/reviews/summary").retrieve().body(Object.class);
        Object menus = restClient.get().uri(menuBaseUrl + "/menus/all").retrieve().body(Object.class);
        return Map.of("sales", sales, "reviews", reviews, "menus", menus);
    }

    // ───────────────────────────────────────────────
    //  ADMIN — 주문 관리
    // ───────────────────────────────────────────────

    @Tag(name = "관리자 — 주문 관리 (Admin Order)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "전체 주문 목록 조회",
        description = "모든 사용자의 주문을 최신순으로 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 주문 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponseDoc.class)))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @GetMapping("/admin/orders")
    public Object adminOrders(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        JwtSupport.UserContext user = requireAdmin(authorization);
        return restClient.get().uri(orderBaseUrl + "/orders")
                .header("X-User-Id", String.valueOf(user.userId()))
                .header("X-Username", user.username())
                .header("X-User-Role", user.role())
                .retrieve()
                .body(Object.class);
    }

    @Tag(name = "관리자 — 주문 관리 (Admin Order)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "주문 상태 변경",
        description = "특정 주문의 상태를 변경합니다. 허용 값: RECEIVED(접수) → COOKING(조리중) → READY(준비완료) → DELIVERED(배달완료)",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"status\":\"COOKING\"}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = OrderStatusUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태값"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
        }
    )
    @PatchMapping("/admin/orders/{id}/status")
    public Object updateOrderStatus(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
            @Parameter(description = "주문 ID") @PathVariable long id,
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        JwtSupport.UserContext user = requireAdmin(authorization);
        // HttpURLConnection does not support PATCH; use PUT internally
        return restClient.put().uri(orderBaseUrl + "/orders/" + id + "/status")
                .header("X-User-Id", String.valueOf(user.userId()))
                .header("X-Username", user.username())
                .header("X-User-Role", user.role())
                .body(body)
                .retrieve()
                .body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  ADMIN — 리뷰 관리
    // ───────────────────────────────────────────────

    @Tag(name = "관리자 — 리뷰 관리 (Admin Review)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "전체 리뷰 목록 조회 (관리자)",
        description = "모든 리뷰를 최신순으로 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 리뷰 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewResponseDoc.class)))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @GetMapping("/admin/reviews")
    public Object adminReviews(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        requireAdmin(authorization);
        return restClient.get().uri(reviewBaseUrl + "/reviews").retrieve().body(Object.class);
    }

    @Tag(name = "관리자 — 리뷰 관리 (Admin Review)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "리뷰 삭제",
        description = "특정 리뷰를 삭제합니다. 부적절한 리뷰를 관리자가 제거할 때 사용합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
        }
    )
    @DeleteMapping("/admin/reviews/{id}")
    public Object deleteReview(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @Parameter(description = "리뷰 ID") @PathVariable long id
    ) {
        requireAdmin(authorization);
        return restClient.delete().uri(reviewBaseUrl + "/reviews/" + id).retrieve().body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  ADMIN — 메뉴 관리
    // ───────────────────────────────────────────────

    @Tag(name = "관리자 — 메뉴 관리 (Admin Menu)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "전체 메뉴 목록 조회 (관리자)",
        description = "판매 중지된 메뉴 포함 전체 메뉴를 반환합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 메뉴 배열 반환",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MenuItemResponse.class)))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @GetMapping("/admin/menus")
    public Object adminMenus(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization
    ) {
        requireAdmin(authorization);
        return restClient.get().uri(menuBaseUrl + "/menus/all").retrieve().body(Object.class);
    }

    @Tag(name = "관리자 — 메뉴 관리 (Admin Menu)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "메뉴 추가",
        description = "새 메뉴를 등록합니다. 요청 바디는 name, description, category, keywords, price, imageUrl, etaMinutes, available 필드를 사용합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"name\":\"새우 로제 파스타\",\"description\":\"크리미한 로제 소스 파스타\",\"category\":\"양식\",\"keywords\":\"새우,크림,파스타\",\"price\":12000,\"imageUrl\":\"https://example.com/pasta.jpg\",\"etaMinutes\":20,\"available\":true}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "201",
                    description = "메뉴 등록 성공",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @PostMapping("/admin/menus")
    @ResponseStatus(HttpStatus.CREATED)
    public Object createMenu(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        requireAdmin(authorization);
        return post(menuBaseUrl + "/menus", body);
    }

    @Tag(name = "관리자 — 메뉴 관리 (Admin Menu)", description = "관리자 전용 — ADMIN 권한 필요")
    @Operation(
        summary = "메뉴 수정",
        description = "기존 메뉴를 수정합니다. id 쿼리 파라미터로 대상 메뉴를 지정하고, body에는 메뉴 서비스 스키마 전체를 전달합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"name\":\"수정된 파스타\",\"description\":\"설명 수정\",\"category\":\"양식\",\"keywords\":\"파스타,크림\",\"price\":15000,\"imageUrl\":\"https://example.com/pasta.jpg\",\"etaMinutes\":22,\"available\":false}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = StatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "메뉴를 찾을 수 없음")
        }
    )
    @PutMapping("/admin/menus")
    public Object updateMenu(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @Parameter(description = "수정할 메뉴 ID") @RequestParam long id,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        requireAdmin(authorization);
        return restClient.put().uri(menuBaseUrl + "/menus/" + id).body(body).retrieve().body(Object.class);
    }

    // ───────────────────────────────────────────────
    //  ADMIN — AI 기능
    // ───────────────────────────────────────────────

    @Tag(name = "관리자 — AI 기능 (Admin AI)", description = "관리자 전용 — AI 기반 메뉴 제안 및 서비스 품질 평가")
    @Operation(
        summary = "AI 신메뉴 제안",
        description = "최근 주문 데이터를 분석하여 AI가 새로운 메뉴를 제안합니다. orders 배열을 전달합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"orders\":[{\"items\":\"김치찌개\",\"totalAmount\":9000}]}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI가 제안한 신메뉴 아이디어 텍스트 반환",
                    content = @Content(schema = @Schema(implementation = SuggestionResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @PostMapping("/admin/new-menu-suggestion")
    public Object suggestNewMenu(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        requireAdmin(authorization);
        return post(aiOperationsBaseUrl + "/suggest-new-menu", body);
    }

    @Tag(name = "관리자 — AI 기능 (Admin AI)", description = "관리자 전용 — AI 기반 메뉴 제안 및 서비스 품질 평가")
    @Operation(
        summary = "AI 서비스 품질 평가",
        description = "고객 리뷰 데이터를 분석하여 AI가 서비스 품질을 종합 평가하고 개선 방향을 제시합니다.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"reviews\":[{\"rating\":4,\"content\":\"맛있어요\"}]}")
            )
        ),
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "AI 서비스 품질 평가 결과 텍스트 반환",
                    content = @Content(schema = @Schema(implementation = QualityResponse.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
        }
    )
    @PostMapping("/admin/service-quality")
    public Object serviceQuality(
        @Parameter(hidden = true) @RequestHeader("Authorization") String authorization,
        @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body
    ) {
        requireAdmin(authorization);
        return post(aiOperationsBaseUrl + "/evaluate-service-quality", body);
    }

    // ───────────────────────────────────────────────
    //  Internal helpers
    // ───────────────────────────────────────────────

    private Object post(String url, Object body) {
        return restClient.post().uri(url).body(body).retrieve().body(Object.class);
    }

    private JwtSupport.UserContext requireUser(String authorization) {
        JwtSupport.UserContext user = jwtSupport.parse(authorization);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "login required");
        }
        return user;
    }

    private JwtSupport.UserContext requireAdmin(String authorization) {
        JwtSupport.UserContext user = requireUser(authorization);
        if (!"ADMIN".equals(user.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin only");
        }
        return user;
    }

    @Schema(name = "AuthTokensResponse")
    record AuthTokensResponse(String accessToken, String refreshToken, AuthUser user) {}

    @Schema(name = "AuthUser")
    record AuthUser(long id, String username, String role) {}

    @Schema(name = "CurrentUserResponse")
    record CurrentUserResponse(long userId, String username, String role) {}

    @Schema(name = "StatusResponse")
    record StatusResponse(String status) {}

    @Schema(name = "CategoryCreateResponse")
    record CategoryCreateResponse(String status, String category) {}

    @Schema(name = "OrderCreateResponse")
    record OrderCreateResponse(long orderId, String status) {}

    @Schema(name = "OrderStatusUpdateResponse")
    record OrderStatusUpdateResponse(long id, String status) {}

    @Schema(name = "MenuItemResponse")
    record MenuItemResponse(
            long id,
            String name,
            String description,
            String category,
            String keywords,
            int price,
            String imageUrl,
            int etaMinutes,
            boolean available) {}

    @Schema(name = "ReviewResponse")
    record ReviewResponseDoc(
            long id,
            long userId,
            String username,
            String menuName,
            String content,
            int rating,
            String createdAt,
            String source) {}

    @Schema(name = "OrderItemResponse")
    record OrderItemResponseDoc(
            long menuId,
            String menuName,
            int quantity,
            int unitPrice,
            int etaMinutes) {}

    @Schema(name = "OrderResponse")
    record OrderResponseDoc(
            long id,
            long userId,
            String username,
            int totalAmount,
            String status,
            String createdAt,
            String notes,
            List<OrderItemResponseDoc> items) {}

    @Schema(name = "ReviewDraftResponse")
    record ReviewDraftResponse(String content, int rating) {}

    @Schema(name = "RecommendMenuResponse")
    record RecommendMenuResponse(List<MenuItemResponse> recommendedMenus, String message) {}

    @Schema(name = "TrafficResponse")
    record TrafficResponse(
            String trafficLevel,
            int estimatedWaitMinutes,
            int currentHour,
            int currentHourOrders,
            String peakHour,
            String message) {}

    @Schema(name = "SalesSummary")
    record SalesSummary(
            int totalSales,
            int todaySales,
            int totalOrders,
            int todayOrders,
            int pendingOrders,
            List<DailySalesPointDoc> dailySales,
            List<HourlyOrderPointDoc> hourlyOrders) {}

    @Schema(name = "DailySalesPoint")
    record DailySalesPointDoc(String day, int totalSales, int totalOrders) {}

    @Schema(name = "HourlyOrderPoint")
    record HourlyOrderPointDoc(int hour, int totalOrders) {}

    @Schema(name = "ReviewSummary")
    record ReviewSummary(double averageRating, int totalReviews, List<RatingDistributionPointDoc> ratingDistribution) {}

    @Schema(name = "RatingDistributionPoint")
    record RatingDistributionPointDoc(int rating, int count) {}

    @Schema(name = "DashboardResponse")
    record DashboardResponse(SalesSummary sales, ReviewSummary reviews, List<MenuItemResponse> menus) {}

    @Schema(name = "SuggestionResponse")
    record SuggestionResponse(String suggestion) {}

    @Schema(name = "QualityResponse")
    record QualityResponse(String analysis) {}
}
