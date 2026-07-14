package com.perflab.login.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void findAllUsersUsesCachedResultAfterFirstLoad() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rows = List.of(Map.of("id", 1, "username", "alice"));
        when(jdbcTemplate.queryForList("SELECT id, username FROM users ORDER BY id")).thenReturn(rows);

        UserService service = new UserService(jdbcTemplate, meterRegistry, objectMapper);

        List<Map<String, Object>> first = service.findAllUsers();
        List<Map<String, Object>> second = service.findAllUsers();

        assertSame(first, second);
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_miss_total").counter().count());
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_hit_total").counter().count());
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_size").gauge().value());
        assertTrue(meterRegistry.get("login_service_users_cache_size_bytes").gauge().value() > 0);
        verify(jdbcTemplate, times(1)).queryForList("SELECT id, username FROM users ORDER BY id");
    }

    @Test
    void addUserRefreshesCacheFromDbForNextRead() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> initialRows = List.of(Map.of("id", 1, "username", "alice"));
        List<Map<String, Object>> refreshedRows = List.of(
                Map.of("id", 1, "username", "alice"),
                Map.of("id", 2, "username", "bob")
        );

        when(jdbcTemplate.queryForList("SELECT id, username FROM users ORDER BY id"))
                .thenReturn(initialRows, refreshedRows);
        when(jdbcTemplate.queryForList("SELECT username FROM users", String.class))
                .thenReturn(List.of("alice"));

        UserService service = new UserService(jdbcTemplate, meterRegistry, objectMapper);

        List<Map<String, Object>> firstRead = service.findAllUsers();
        double cacheSizeBytesAfterFirstRead = meterRegistry.get("login_service_users_cache_size_bytes").gauge().value();
        Map<String, Object> addResult = service.addUser("bob");
        List<Map<String, Object>> readAfterWrite = service.findAllUsers();
        double cacheSizeBytesAfterRefresh = meterRegistry.get("login_service_users_cache_size_bytes").gauge().value();

        assertEquals("bob", addResult.get("username"));
        assertEquals(initialRows, firstRead);
        assertEquals(refreshedRows, readAfterWrite);
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_refresh_total").counter().count());
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_hit_total").counter().count());
        assertEquals(1.0, meterRegistry.get("login_service_users_cache_miss_total").counter().count());
        assertEquals(2.0, meterRegistry.get("login_service_users_cache_size").gauge().value());
        assertTrue(cacheSizeBytesAfterFirstRead > 0);
        assertTrue(cacheSizeBytesAfterRefresh > cacheSizeBytesAfterFirstRead);

        verify(jdbcTemplate, times(2)).queryForList("SELECT id, username FROM users ORDER BY id");
        verify(jdbcTemplate, times(1)).queryForList("SELECT username FROM users", String.class);
        verify(jdbcTemplate, times(1)).update("INSERT INTO users (username) VALUES (?)", "bob");
    }
}
