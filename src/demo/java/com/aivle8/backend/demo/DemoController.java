package com.aivle8.backend.demo;

import com.aivle8.backend.global.common.BaseResponse;
import com.aivle8.backend.repository.UserRepository;
import com.aivle8.backend.service.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Profile("demo")
@RestController
@RequestMapping("/api/v1/auth/demo")
public class DemoController {
    private final UserRepository users;
    private final JwtService jwt;
    private final JdbcTemplate database;
    private final Map<String, byte[]> objects;

    public DemoController(UserRepository users, JwtService jwt, JdbcTemplate database, Map<String, byte[]> demoObjects) {
        this.users = users; this.jwt = jwt; this.database = database; this.objects = demoObjects;
    }

    @PostMapping("/session")
    public BaseResponse<Map<String, String>> session(@RequestParam(defaultValue = "author") String persona) {
        if (!persona.equals("author") && !persona.equals("reader")) throw new IllegalArgumentException("Unknown demo persona");
        var user = users.findByEmail(persona + "@example.test").orElseThrow();
        return BaseResponse.success(Map.of("token", jwt.createToken(user)));
    }

    @GetMapping(value = "/covers/ai-images/{file}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> cover(@PathVariable String file) {
        byte[] bytes = objects.get("ai-images/" + file);
        return bytes == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(bytes);
    }

    /** Read-only SQL observation for local full-stack tests; absent from production JAR. */
    @GetMapping("/database/books/{id}")
    public Map<String, Object> row(@PathVariable long id) {
        var rows = database.query("SELECT b.book_id, b.title, b.category, b.cover_image_url, b.user_id, d.description, d.content FROM books b JOIN book_details d ON d.book_id = b.book_id WHERE b.book_id = ?",
                (row, index) -> Map.<String, Object>of("book_id", row.getLong("book_id"), "title", row.getString("title"),
                        "category", row.getString("category"), "cover_image_url", row.getString("cover_image_url"),
                        "user_id", row.getLong("user_id"), "description", row.getString("description"), "content", row.getString("content")), id);
        return rows.isEmpty() ? Map.of("exists", false) : rows.get(0);
    }
}
