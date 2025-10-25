package com.msanchez.url_shortener.domain.repositories;

import com.msanchez.url_shortener.domain.entities.ShortURL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortURL, Long> {

    @Query("select su from ShortURL su left join fetch su.createdBy where su.isPrivate = false")
    Page<ShortURL> findPublicShortUrls(Pageable pageable);

    boolean existsByShortKey(String shortKey);

    Optional<ShortURL> findByShortKey(String shortKey);

    Page<ShortURL> findByCreatedById(Long userId, Pageable pageable);

    @Modifying
    void deleteByIdInAndCreatedById(List<Long> ids, Long userId);

    @Query("select u from ShortURL u left join fetch u.createdBy")
    Page<ShortURL> findAllShortUrls(Pageable pageable);
}
