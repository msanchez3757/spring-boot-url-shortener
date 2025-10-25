package com.msanchez.url_shortener.domain.models;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

public record ShortUrlDTO(Long id, String shortKey, String originalUrl,
                          Boolean isPrivate, LocalDateTime expiresAt,
                          UserDTO createdBy, Long clickCount,
                          LocalDateTime createdAt) implements Serializable {
}