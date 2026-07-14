package com.perflab.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> cachedUsers;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter cacheRefreshCounter;

    public UserService(JdbcTemplate jdbcTemplate, MeterRegistry meterRegistry, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.cacheHitCounter = meterRegistry.counter("login_service_users_cache_hit_total");
        this.cacheMissCounter = meterRegistry.counter("login_service_users_cache_miss_total");
        this.cacheRefreshCounter = meterRegistry.counter("login_service_users_cache_refresh_total");
        Gauge.builder("login_service_users_cache_size", this, UserService::cacheEntryCount)
                .description("Number of users currently stored in in-memory users cache")
                .register(meterRegistry);
        Gauge.builder("login_service_users_cache_size_bytes", this, UserService::cacheSizeBytes)
                .description("Approximate cache size in bytes based on JSON payload size")
                .baseUnit("bytes")
                .register(meterRegistry);
    }

    public synchronized List<Map<String, Object>> findAllUsers() {
        if (cachedUsers != null) {
            cacheHitCounter.increment();
            return cachedUsers;
        }

        cacheMissCounter.increment();
        cachedUsers = loadAllUsersFromDb();
        return cachedUsers;
    }

    public synchronized Map<String, Object> addUser(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }

        String requestedUsername = username.trim();
        List<String> existingUsernames = jdbcTemplate.queryForList(
                "SELECT username FROM users",
                String.class
        );

        Set<String> existingUsernamesSet = existingUsernames.stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        String finalUsername = resolveUniqueUsername(requestedUsername, existingUsernamesSet);
        jdbcTemplate.update("INSERT INTO users (username) VALUES (?)", finalUsername);
        cachedUsers = loadAllUsersFromDb();
        cacheRefreshCounter.increment();
        return Map.of("username", finalUsername);
    }

    private List<Map<String, Object>> loadAllUsersFromDb() {
        List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id, username FROM users ORDER BY id");
        List<Map<String, Object>> snapshot = new java.util.ArrayList<>(users.size());
        for (Map<String, Object> user : users) {
            snapshot.add(Map.copyOf(user));
        }
        return List.copyOf(snapshot);
    }

    private double cacheSizeBytes() {
        if (cachedUsers == null) {
            return 0;
        }

        try {
            return objectMapper.writeValueAsBytes(cachedUsers).length;
        } catch (JsonProcessingException ex) {
            return 0;
        }
    }

    private double cacheEntryCount() {
        return cachedUsers == null ? 0 : cachedUsers.size();
    }

    private String resolveUniqueUsername(String requestedUsername, Set<String> existingUsernames) {
        if (!existingUsernames.contains(requestedUsername)) {
            return requestedUsername;
        }

        String candidate = requestedUsername;
        while (existingUsernames.contains(candidate)) {
            candidate = requestedUsername + "_" + randomAlphanumeric(6);
        }
        return candidate;
    }

    private String randomAlphanumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return builder.toString();
    }
}