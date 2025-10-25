package com.msanchez.url_shortener.domain.services;

import com.msanchez.url_shortener.ApplicationProperties;
import com.msanchez.url_shortener.domain.entities.ShortURL;
import com.msanchez.url_shortener.domain.models.CreateShortUrlCmd;
import com.msanchez.url_shortener.domain.models.PagedResult;
import com.msanchez.url_shortener.domain.models.ShortUrlDTO;
import com.msanchez.url_shortener.domain.repositories.ShortUrlRepository;
import com.msanchez.url_shortener.domain.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final EntityMapper entityMapper;
    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;

    public ShortUrlService(ShortUrlRepository shortUrlRepository, EntityMapper entityMapper, ApplicationProperties applicationProperties,
                           UserRepository userRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.entityMapper = entityMapper;
        this.applicationProperties = applicationProperties;
        this.userRepository = userRepository;
    }

    public PagedResult<ShortUrlDTO> findAllPublicShortUrls(int pageNo, int pageSize){
        pageNo = pageNo > 1? pageNo - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ShortUrlDTO> shortUrlDtoPage = shortUrlRepository.findPublicShortUrls(pageable)
                .map(entityMapper::toShortUrlDTO);
        return PagedResult.from(shortUrlDtoPage);
    }

    @Transactional
    public ShortUrlDTO createShortUrl(CreateShortUrlCmd cmd){
        if(applicationProperties.validateOriginalUrl()){
            boolean urlExists = UrlExistenceValidator.isUrlExists(cmd.originalUrl());
            if(!urlExists){
                throw new RuntimeException("Invalid URL");
            }
        }
        var shortKey = generateRandomShortKey();
        var shortUrl = new ShortURL();
        shortUrl.setOriginalUrl(cmd.originalUrl());
        shortUrl.setShortKey(shortKey);
        if(cmd.userId() == null){
            shortUrl.setCreatedBy(null);
            shortUrl.setPrivate(false);
            shortUrl.setExpiresAt(LocalDateTime.now().plus(applicationProperties.defaultExpiryInDays(), DAYS));
        } else{
            shortUrl.setCreatedBy(userRepository.findById(cmd.userId()).orElseThrow());
            shortUrl.setPrivate(cmd.isPrivate() != null && cmd.isPrivate());
            shortUrl.setExpiresAt(cmd.expirationInDays() != null ? LocalDateTime.now().plus(cmd.expirationInDays(), DAYS) : null);
        }
        shortUrl.setClickCount(0L);
        shortUrl.setCreatedAt(LocalDateTime.now());
        shortUrlRepository.save(shortUrl);

        return entityMapper.toShortUrlDTO(shortUrl);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateRandomShortKey();
        } while (shortUrlRepository.existsByShortKey(shortKey));
        return shortKey;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomShortKey() {
        StringBuilder sb = new StringBuilder(SHORT_KEY_LENGTH);
        for (int i = 0; i < SHORT_KEY_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Transactional
    public Optional<ShortUrlDTO> accessShortUrl(String shortKey, Long userId) {
        Optional<ShortURL> shortURLOptional = shortUrlRepository.findByShortKey(shortKey);
        if(shortURLOptional.isEmpty()){
            return Optional.empty();
        }
        ShortURL shortURL = shortURLOptional.get();
        if(shortURL.getExpiresAt() != null && shortURL.getExpiresAt().isBefore(LocalDateTime.now())){
            return Optional.empty();
        }
        if(shortURL.getPrivate() != null && shortURL.getPrivate()
                && shortURL.getCreatedBy() != null
                && !Objects.equals(shortURL.getCreatedBy().getId(), userId)) {
            return Optional.empty();
        }
        shortURL.setClickCount(shortURL.getClickCount()+1);
        shortUrlRepository.save(shortURL);
        return shortURLOptional.map(entityMapper::toShortUrlDTO);
    }

    public PagedResult<ShortUrlDTO> getUserShortUrls(Long userId, int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage = shortUrlRepository.findByCreatedById(userId, pageable)
                .map(entityMapper::toShortUrlDTO);
        return PagedResult.from(shortUrlsPage);
    }

    private Pageable getPageable(int page, int size) {
        page = page > 1 ? page - 1: 0;
        return PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
    }

    @Transactional
    public void deleteUserShortUrls(List<Long> ids, Long userId) {
        if (ids != null && !ids.isEmpty() && userId != null) {
            shortUrlRepository.deleteByIdInAndCreatedById(ids, userId);
        }
    }

    public PagedResult<ShortUrlDTO> findAllShortUrls(int page, int pageSize) {
        Pageable pageable = getPageable(page, pageSize);
        var shortUrlsPage =  shortUrlRepository.findAllShortUrls(pageable).map(entityMapper::toShortUrlDTO);
        return PagedResult.from(shortUrlsPage);
    }
}
