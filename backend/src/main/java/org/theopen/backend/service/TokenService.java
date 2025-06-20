package org.theopen.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class TokenService {

    @Value("${security.jwt.token.secret-key:secureTheOpenVPNKeyDefaultValueWith32Chars}")
    private String secretKey;

    @Value("${security.jwt.token.access-expiration:86400}")
    private long accessExpirationSeconds;

    /**
     * Генерирует токен доступа для пользователя
     *
     * @param tgId идентификатор пользователя Telegram
     * @return JWT токен
     */
    public String generateAccessToken(Long tgId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tgId", tgId);
        return createToken(claims, tgId.toString(), accessExpirationSeconds);
    }

    /**
     * Создает JWT токен с указанными параметрами
     *
     * @param claims дополнительные данные токена
     * @param subject тема токена (обычно идентификатор пользователя)
     * @param expirationSeconds время жизни токена в секундах
     * @return JWT токен
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationSeconds) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(Duration.ofSeconds(expirationSeconds));

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Проверяет валидность токена для указанного пользователя
     *
     * @param token JWT токен
     * @param tgId идентификатор пользователя Telegram
     * @return true если токен действителен для данного пользователя
     */
    public boolean validateToken(String token, Long tgId) {
        if (token == null || token.isBlank() || token.length() < 10) {
            return false;
        }

        // Если токен начинается с "Bearer ", убираем эту часть
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            final String tgIdFromToken = extractClaim(token, claims -> claims.get("tgId").toString());
            return tgIdFromToken.equals(tgId.toString()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Ошибка валидации токена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, не истек ли срок действия токена
     *
     * @param token JWT токен
     * @return true если токен просрочен
     */
    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    /**
     * Извлекает дату истечения срока действия из токена
     *
     * @param token JWT токен
     * @return дата истечения срока действия
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает данные из токена с помощью указанной функции
     *
     * @param token JWT токен
     * @param claimsResolver функция для извлечения нужных данных
     * @param <T> тип возвращаемых данных
     * @return извлеченные данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все данные из токена
     *
     * @param token JWT токен
     * @return claims из токена
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
