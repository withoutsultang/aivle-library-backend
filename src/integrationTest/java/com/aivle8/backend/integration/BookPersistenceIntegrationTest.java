package com.aivle8.backend.integration;

import com.aivle8.backend.BackendApplication;
import com.aivle8.backend.repository.UserRepository;
import com.aivle8.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class BookPersistenceIntegrationTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate database;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;

    private String bearer(String persona) {
        return "Bearer " + jwt.createToken(users.findByEmail(persona + "@example.test").orElseThrow());
    }
    private String book(String title, String content) throws Exception {
        return json.writeValueAsString(Map.of("title", title, "category", "에세이", "description", "실제 SQL 저장 검증", "content", content));
    }

    @Test void createsUpdatesAndDeletesActualRelationalRows() throws Exception {
        var response = http.perform(post("/api/v1/books").header("Authorization", bearer("author"))
                        .contentType(MediaType.APPLICATION_JSON).content(book("SQL에 저장한 도서", "처음 저장한 본문")))
                .andExpect(status().isCreated()).andReturn().getResponse();
        long id = json.readTree(response.getContentAsString()).path("data").path("bookId").asLong();
        assertEquals("SQL에 저장한 도서", database.queryForObject("SELECT title FROM books WHERE book_id = ?", String.class, id));
        assertEquals("처음 저장한 본문", database.queryForObject("SELECT content FROM book_details WHERE book_id = ?", String.class, id));
        http.perform(put("/api/v1/books/" + id).header("Authorization", bearer("author"))
                        .contentType(MediaType.APPLICATION_JSON).content(book("수정한 도서", "SQL에서 확인한 수정 본문")))
                .andExpect(status().isOk());
        assertEquals("SQL에서 확인한 수정 본문", database.queryForObject("SELECT content FROM book_details WHERE book_id = ?", String.class, id));
        http.perform(delete("/api/v1/books/" + id).header("Authorization", bearer("author"))).andExpect(status().isOk());
        assertEquals(0, database.queryForObject("SELECT COUNT(*) FROM books WHERE book_id = ?", Integer.class, id));
        assertEquals(0, database.queryForObject("SELECT COUNT(*) FROM book_details WHERE book_id = ?", Integer.class, id));
    }

    @Test void rejectsAnotherAuthorWithoutChangingStoredText() throws Exception {
        String original = database.queryForObject("SELECT title FROM books WHERE book_id = 1", String.class);
        http.perform(put("/api/v1/books/1").header("Authorization", bearer("reader"))
                        .contentType(MediaType.APPLICATION_JSON).content(book("타인의 변경", "허용되지 않은 본문")))
                .andExpect(status().isForbidden());
        assertEquals(original, database.queryForObject("SELECT title FROM books WHERE book_id = 1", String.class));
    }

    @Test void rejectsInvalidTokenAndDtoBeforePersistence() throws Exception {
        long before = database.queryForObject("SELECT COUNT(*) FROM books", Long.class);
        http.perform(post("/api/v1/books").header("Authorization", "Bearer invalid-fixture")
                        .contentType(MediaType.APPLICATION_JSON).content(book("잘못된 인증", "본문")))
                .andExpect(status().isUnauthorized());
        http.perform(post("/api/v1/books").header("Authorization", bearer("author"))
                        .contentType(MediaType.APPLICATION_JSON).content(book("", "본문")))
                .andExpect(status().isBadRequest());
        assertEquals(before, database.queryForObject("SELECT COUNT(*) FROM books", Long.class));
    }
}
