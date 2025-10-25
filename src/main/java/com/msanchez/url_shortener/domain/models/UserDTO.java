package com.msanchez.url_shortener.domain.models;

import java.io.Serializable;

public record UserDTO(Long id, String name) implements Serializable {
}
