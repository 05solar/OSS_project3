package com.example.menu;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menus")
public class MenuController {
    private final JdbcClient jdbc;

    public MenuController(JdbcClient jdbc) {
        this.jdbc = jdbc;
        initSchema();
        seedMenus();
    }

    @GetMapping
    public List<MenuItem> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        String sql = "select * from menus where available = 1";
        if (category != null && !category.isBlank()) {
            sql += " and category = :category";
        }
        if (keyword != null && !keyword.isBlank()) {
            sql += " and lower(keywords) like lower(:keyword)";
        }
        var statement = jdbc.sql(sql + " order by category, name");
        if (category != null && !category.isBlank()) {
            statement.param("category", category);
        }
        if (keyword != null && !keyword.isBlank()) {
            statement.param("keyword", "%" + keyword + "%");
        }
        return statement.query(this::mapMenu).list();
    }

    @GetMapping("/all")
    public List<MenuItem> listAll() {
        return jdbc.sql("select * from menus order by category, name").query(this::mapMenu).list();
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return jdbc.sql("select distinct category from menus order by category").query(String.class).list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody MenuRequest request) {
        jdbc.sql("""
                insert into menus(name, description, category, keywords, price, image_url, eta_minutes, available)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """).params(List.of(
                        request.name(), request.description(), request.category(), request.keywords(),
                        request.price(), request.imageUrl(), request.etaMinutes(), request.available() ? 1 : 0))
                .update();
        return Map.of("status", "created");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable long id, @RequestBody MenuRequest request) {
        jdbc.sql("""
                update menus
                set name = ?, description = ?, category = ?, keywords = ?, price = ?, image_url = ?, eta_minutes = ?, available = ?
                where id = ?
                """).params(List.of(
                        request.name(), request.description(), request.category(), request.keywords(), request.price(),
                        request.imageUrl(), request.etaMinutes(), request.available() ? 1 : 0, id))
                .update();
        return Map.of("status", "updated");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable long id) {
        jdbc.sql("delete from menus where id = ?").param(id).update();
        return Map.of("status", "deleted");
    }

    private MenuItem mapMenu(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new MenuItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getString("keywords"),
                rs.getInt("price"),
                rs.getString("image_url"),
                rs.getInt("eta_minutes"),
                rs.getInt("available") == 1
        );
    }

    private void initSchema() {
        jdbc.sql("""
                create table if not exists menus (
                  id integer primary key autoincrement,
                  name text not null,
                  description text,
                  category text not null,
                  keywords text,
                  price integer not null,
                  image_url text,
                  eta_minutes integer not null default 15,
                  available integer not null default 1
                )
                """).update();
    }

    private void seedMenus() {
        Long count = jdbc.sql("select count(*) from menus").query(Long.class).single();
        if (count != null && count == 0) {
            List<MenuRequest> seeds = List.of(
                    // 양식
                    new MenuRequest("트러플 크림 파스타", "트러플 향이 풍부한 고급 크림 파스타", "양식", "트러플,크림,파스타,고급,데이트", 19000, "https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5", 18, true),
                    new MenuRequest("안심 스테이크", "부드러운 안심으로 만든 프리미엄 스테이크", "양식", "스테이크,고기,고급,특별한날", 35000, "https://images.unsplash.com/photo-1546833998-877b37c2e5c6", 25, true),
                    new MenuRequest("해산물 토마토 파스타", "신선한 해산물과 토마토 소스의 파스타", "양식", "해산물,토마토,파스타,새우,홍합", 16000, "https://images.unsplash.com/photo-1563379926898-05f4575a45d8", 18, true),
                    new MenuRequest("마르게리타 피자", "신선한 토마토 소스와 모짜렐라 피자", "양식", "피자,치즈,토마토,이탈리안", 15000, "https://images.unsplash.com/photo-1513104890138-7c749659a591", 20, true),
                    // 음료
                    new MenuRequest("아메리카노", "깔끔하고 진한 에스프레소 아메리카노", "음료", "커피,아메리카노,시원함,디저트", 4500, "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085", 5, true),
                    new MenuRequest("카페라떼", "부드러운 우유 거품과 에스프레소", "음료", "커피,라떼,부드러움,디저트", 5500, "https://images.unsplash.com/photo-1561882468-9110d70d03a0", 5, true),
                    new MenuRequest("레몬에이드", "상큼한 레몬 에이드", "음료", "레몬,상큼,시원함,비커피", 6000, "https://images.unsplash.com/photo-1556679343-c7306c1976bc", 5, true),
                    new MenuRequest("생과일 주스", "신선한 제철 과일로 만든 주스", "음료", "과일,건강,신선함,비커피", 7000, "https://images.unsplash.com/photo-1589733955941-5eeaf752f6dd", 7, true),
                    // 일식
                    new MenuRequest("연어 사시미", "신선한 연어 회", "일식", "연어,회,신선함,해산물,고급", 22000, "https://images.unsplash.com/photo-1617196034183-421b4040ed20", 10, true),
                    new MenuRequest("돈카츠 정식", "바삭한 돈카츠와 밥·된장국 세트", "일식", "돈카츠,바삭,정식,아이동반", 14000, "https://images.unsplash.com/photo-1604908176997-431f3d4cfe3a", 15, true),
                    new MenuRequest("규동", "부드러운 소고기 덮밥", "일식", "소고기,덮밥,든든함,간단", 12000, "https://images.unsplash.com/photo-1547592180-85f173990554", 12, true),
                    new MenuRequest("새우 튀김 우동", "탱탱한 우동면과 바삭한 새우 튀김", "일식", "우동,새우,튀김,따뜻함,국물", 11000, "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d", 13, true),
                    // 한식
                    new MenuRequest("된장찌개", "구수한 된장 국물의 한국 전통 찌개", "한식", "된장,국물,따뜻함,구수함", 8000, "https://images.unsplash.com/photo-1583224964978-2f4f8b5c6b1f", 12, true),
                    new MenuRequest("제육볶음", "매콤달콤한 돼지고기 볶음", "한식", "돼지고기,매콤,볶음,든든함", 10000, "https://images.unsplash.com/photo-1569050467447-ce54b3bbc37d", 12, true),
                    new MenuRequest("갈비탕", "진한 사골 국물의 갈비탕", "한식", "갈비,국물,따뜻함,고급,특별한날", 14000, "https://images.unsplash.com/photo-1547592180-85f173990554", 20, true),
                    new MenuRequest("비빔밥", "신선한 채소와 함께 비벼 먹는 영양 한식", "한식", "채소,건강,비빔밥,든든함", 9000, "https://images.unsplash.com/photo-1553163147-622ab57be1c7", 10, true)
            );
            for (MenuRequest item : seeds) {
                create(item);
            }
        }
    }

    record MenuRequest(String name, String description, String category, String keywords, int price, String imageUrl, int etaMinutes, boolean available) {}
    record MenuItem(long id, String name, String description, String category, String keywords, int price, String imageUrl, int etaMinutes, boolean available) {}
}

