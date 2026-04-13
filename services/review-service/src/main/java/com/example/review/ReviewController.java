package com.example.review;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final JdbcClient jdbc;

    public ReviewController(JdbcClient jdbc) {
        this.jdbc = jdbc;
        initSchema();
        seedReviews();
    }

    @GetMapping
    public List<ReviewResponse> list() {
        return jdbc.sql("select * from reviews order by created_at desc").query((rs, rowNum) -> new ReviewResponse(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("menu_name"),
                rs.getString("content"),
                rs.getInt("rating"),
                rs.getString("created_at"),
                rs.getString("source"))).list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestHeader("X-User-Id") long userId,
                                      @RequestHeader("X-Username") String username,
                                      @RequestBody CreateReviewRequest request) {
        jdbc.sql("""
                insert into reviews(user_id, username, menu_name, content, rating, created_at, source)
                values (?, ?, ?, ?, ?, ?, ?)
                """).params(List.of(userId, username, request.menuName(), request.content(), request.rating(), Instant.now().toString(), request.source()))
                .update();
        return Map.of("status", "created");
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Double avgRating = jdbc.sql("select coalesce(avg(rating), 0) from reviews").query(Double.class).single();
        Integer totalReviews = jdbc.sql("select count(*) from reviews").query(Integer.class).single();
        return Map.of("averageRating", avgRating == null ? 0.0 : avgRating, "totalReviews", totalReviews == null ? 0 : totalReviews);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable long id) {
        int deleted = jdbc.sql("delete from reviews where id = ?").param(id).update();
        return Map.of("status", deleted > 0 ? "deleted" : "not_found");
    }

    private void initSchema() {
        jdbc.sql("""
                create table if not exists reviews (
                  id integer primary key autoincrement,
                  user_id integer not null,
                  username text not null,
                  menu_name text not null,
                  content text not null,
                  rating integer not null,
                  created_at text not null,
                  source text not null
                )
                """).update();
    }

    private void seedReviews() {
        Long count = jdbc.sql("select count(*) from reviews").query(Long.class).single();
        if (count != null && count == 0) {
            Object[][] seeds = {
                {2L, "김민준", "안심 스테이크", "육즙이 살아있고 굽기도 딱 맞았어요. 특별한 날 다시 오고 싶네요.", 5},
                {3L, "이서연", "트러플 크림 파스타", "트러플 향이 은은하게 퍼지면서 크림이 정말 부드러워요. 최고의 파스타였어요!", 5},
                {4L, "최유나", "된장찌개", "집밥 같은 구수한 맛이에요. 밥 한 그릇 뚝딱 비웠네요. 자주 올 것 같아요.", 5},
                {5L, "한소율", "마르게리타 피자", "도우가 얇고 바삭해서 토마토 소스와 치즈의 조화가 완벽해요!", 5},
                {6L, "강태양", "갈비탕", "국물이 깊고 진해서 추운 날 먹기 최고였어요. 고기도 무르게 잘 익었네요.", 5},
                {7L, "윤하린", "해산물 토마토 파스타", "새우와 홍합이 가득하고 토마토 소스가 새콤달콤해요. 다음에 또 주문할게요!", 5},
                {8L, "박지호", "연어 사시미", "연어가 신선하고 두툼하게 썰려 있어서 만족했어요. 간장 소스도 잘 어울렸습니다.", 4},
                {9L, "정승현", "비빔밥", "채소가 싱싱하고 고추장 양념이 딱 맞아요. 건강한 한 끼로 추천합니다.", 4},
                {10L, "오지민", "카페라떼", "우유 거품이 부드럽고 에스프레소도 진하지 않아 딱 좋았어요.", 4},
                {11L, "임채원", "돈카츠 정식", "튀김옷이 바삭하고 두툼한 고기가 인상적이었어요. 밑반찬도 충실했습니다.", 4}
            };
            for (Object[] s : seeds) {
                jdbc.sql("""
                        insert into reviews(user_id, username, menu_name, content, rating, created_at, source)
                        values (?, ?, ?, ?, ?, ?, 'seed')
                        """).params(List.of(s[0], s[1], s[2], s[3], s[4], Instant.now().toString())).update();
            }
        }
    }

    record CreateReviewRequest(String menuName, String content, int rating, String source) {}
    record ReviewResponse(long id, long userId, String username, String menuName, String content, int rating, String createdAt, String source) {}
}

