package com.msanchez.url_shortener.domain.services;

import com.msanchez.url_shortener.domain.entities.ShortURL;
import com.msanchez.url_shortener.domain.entities.User;
import com.msanchez.url_shortener.domain.models.ShortUrlDTO;
import com.msanchez.url_shortener.domain.models.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public ShortUrlDTO toShortUrlDTO(ShortURL shortURL){
        UserDTO userDTO = null;
        if(shortURL.getCreatedBy() != null){
            userDTO = toUserDTO(shortURL.getCreatedBy());
        }
        return new ShortUrlDTO(
                shortURL.getId(),
                shortURL.getShortKey(),
                shortURL.getOriginalUrl(),
                shortURL.getPrivate(),
                shortURL.getExpiresAt(),
                userDTO,
                shortURL.getClickCount(),
                shortURL.getCreatedAt()
        );
    }

    public UserDTO toUserDTO(User user){
        return new UserDTO(user.getId(), user.getName());
    }
}
