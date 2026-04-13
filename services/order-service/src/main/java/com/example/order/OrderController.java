package com.example.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final JdbcClient jdbc;

    public OrderController(JdbcClient jdbc) {
        this.jdbc = jdbc;
        initSchema();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(
            @RequestHeader("X-User-Id") long userId,
            @RequestHeader("X-Username") String username,
            @RequestBody CreateOrderRequest request) {
        jdbc.sql("insert into orders(user_id, username, total_amount, status, created_at, notes) values (?, ?, ?, ?, ?, ?)")
                .params(List.of(userId, username, request.totalAmount(), "RECEIVED", Instant.now().toString(), request.notes()))
                .update();
        long orderId = jdbc.sql("select last_insert_rowid()").query(Long.class).single();
        for (OrderItemRequest item : request.items()) {
            jdbc.sql("""
                    insert into order_items(order_id, menu_id, menu_name, quantity, unit_price, eta_minutes)
                    values (?, ?, ?, ?, ?, ?)
                    """).params(List.of(orderId, item.menuId(), item.menuName(), item.quantity(), item.unitPrice(), item.etaMinutes()))
                    .update();
        }
        return Map.of("orderId", orderId, "status", "RECEIVED");
    }

    @GetMapping
    public List<OrderResponse> list(@RequestHeader(value = "X-User-Role", required = false) String role,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                    @RequestParam(required = false) String status) {
        String sql = "select * from orders where 1=1";
        if (!"ADMIN".equals(role)) {
            sql += " and user_id = :userId";
        }
        if (status != null && !status.isBlank()) {
            sql += " and status = :status";
        }
        var statement = jdbc.sql(sql + " order by created_at desc");
        if (!"ADMIN".equals(role)) {
            statement.param("userId", userId);
        }
        if (status != null && !status.isBlank()) {
            statement.param("status", status);
        }
        List<OrderResponse> orders = statement.query((rs, rowNum) -> new OrderResponse(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getInt("total_amount"),
                rs.getString("status"),
                rs.getString("created_at"),
                rs.getString("notes"),
                new ArrayList<>())).list();
        for (OrderResponse order : orders) {
            order.items().addAll(jdbc.sql("select * from order_items where order_id = ?")
                    .param(order.id())
                    .query((rs, rowNum) -> new OrderItemResponse(
                            rs.getLong("menu_id"),
                            rs.getString("menu_name"),
                            rs.getInt("quantity"),
                            rs.getInt("unit_price"),
                            rs.getInt("eta_minutes")))
                    .list());
        }
        return orders;
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable long id,
            @RequestBody Map<String, String> body) {
        if (!"ADMIN".equals(role)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }
        String newStatus = body.get("status");
        List<String> allowed = List.of("RECEIVED", "COOKING", "READY", "DELIVERED");
        if (newStatus == null || !allowed.contains(newStatus)) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        int updated = jdbc.sql("update orders set status = ? where id = ?")
                .params(List.of(newStatus, id))
                .update();
        if (updated == 0) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return Map.of("id", id, "status", newStatus);
    }

    @GetMapping("/summary")
    public Map<String, Object> salesSummary() {
        Integer totalSales = jdbc.sql("select coalesce(sum(total_amount), 0) from orders").query(Integer.class).single();
        Integer totalOrders = jdbc.sql("select count(*) from orders").query(Integer.class).single();
        Integer pendingOrders = jdbc.sql("select count(*) from orders where status in ('RECEIVED', 'COOKING')").query(Integer.class).single();
        return Map.of(
                "totalSales", totalSales == null ? 0 : totalSales,
                "totalOrders", totalOrders == null ? 0 : totalOrders,
                "pendingOrders", pendingOrders == null ? 0 : pendingOrders
        );
    }

    private void initSchema() {
        jdbc.sql("""
                create table if not exists orders (
                  id integer primary key autoincrement,
                  user_id integer not null,
                  username text not null,
                  total_amount integer not null,
                  status text not null,
                  created_at text not null,
                  notes text
                )
                """).update();
        jdbc.sql("""
                create table if not exists order_items (
                  id integer primary key autoincrement,
                  order_id integer not null,
                  menu_id integer not null,
                  menu_name text not null,
                  quantity integer not null,
                  unit_price integer not null,
                  eta_minutes integer not null
                )
                """).update();
    }

    record CreateOrderRequest(List<OrderItemRequest> items, int totalAmount, String notes) {}
    record OrderItemRequest(long menuId, String menuName, int quantity, int unitPrice, int etaMinutes) {}
    record OrderItemResponse(long menuId, String menuName, int quantity, int unitPrice, int etaMinutes) {}
    record OrderResponse(long id, long userId, String username, int totalAmount, String status, String createdAt, String notes, List<OrderItemResponse> items) {}
}

