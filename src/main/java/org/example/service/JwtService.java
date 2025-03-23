package org.example.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервисный класс для работы с JWT токенами.
 * <p>
 * Предоставляет методы для генерации, валидации и парсинга JWT.
 * Использует секретный ключ и время жизни токена, заданные в application.properties.

 * Основан на библиотеке io.jsonwebtoken (JJWT).
 * </p>
 *
 */

@Service
public class JwtService {

    /**
     * Секретный ключ для подписи и проверки JWT.
     * Значение берётся из application.properties: jwt.secret.
     */

    @Value("${jwt.secret}")
    String secretKey;

    /**
     * Время жизни JWT токена в миллисекундах.
     * Значение берётся из application.properties: jwt.expiration.
     */

    @Value("${jwt.expiration}")
    long jwtExpirationMs;

    /**
     * Генерирует JWT токен для переданного пользователя.
     *
     * @param userDetails пользователь, для которого создаётся токен
     * @return сгенерированный JWT токен в виде строки
     */

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Генерирует JWT токен с дополнительными claims.
     *
     * @param extraClaims   дополнительные claims, которые будут добавлены в payload токена
     * @param userDetails   пользователь, для которого создаётся токен
     * @return сгенерированный JWT токен в виде строки
     */

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает имя пользователя (subject) из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя, сохранённое в токене
     */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Проверяет, действителен ли токен для переданного пользователя.
     *
     * @param token        JWT токен
     * @param userDetails  пользователь, с которым сверяется subject токена
     * @return true, если токен валиден и не истёк, иначе false
     */

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Проверяет, истёк ли срок действия токена.
     *
     * @param token JWT токен
     * @return true, если срок действия истёк, иначе false
     */

    boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token JWT токен
     * @return дата истечения срока действия
     */

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает конкретный claim из токена, используя переданный claimsResolver.
     *
     * @param <T>            тип возвращаемого значения
     * @param token          JWT токен
     * @param claimsResolver функция для извлечения необходимого значения из claims
     * @return значение claim-а
     */

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все claims из JWT токена.
     *
     * @param token JWT токен
     * @return объект Claims, содержащий все данные из токена
     */

    Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Возвращает ключ подписи на основе секретного ключа.
     * Секретный ключ декодируется из BASE64.
     *
     * @return ключ, используемый для подписи и проверки JWT
     */

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
