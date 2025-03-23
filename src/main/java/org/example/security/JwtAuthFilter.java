package org.example.security;

import org.example.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT фильтр для проверки токенов в каждом HTTP-запросе.
 * <p>
 * Наследуется от {@link OncePerRequestFilter}, что гарантирует выполнение фильтра только один раз на запрос.
 * Извлекает токен из заголовка "Authorization", валидирует его и устанавливает пользователя в {@link SecurityContextHolder}.
 * </p>
 *
 * <p><b>Примечание:</b> Ожидается, что токен передаётся в формате "Bearer {token}"</p>
 *

 */

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    /**
     * Сервис для работы с JWT токенами (генерация, валидация, извлечение данных).
     */
    private final JwtService jwtService;

    /**
     * Сервис загрузки информации о пользователях (реализует {@link UserDetailsService}).
     */
    private final UserDetailsService userDetailsService;

    /**
     * Менеджер аутентификации (пока не используется в этом фильтре, но может быть применён при кастомной логике аутентификации).
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Основной метод фильтра, обрабатывающий входящий запрос.
     *
     * @param request     входящий HTTP запрос
     * @param response    HTTP ответ
     * @param filterChain цепочка фильтров для продолжения обработки запроса
     * @throws ServletException возможная ошибка сервлета
     * @throws IOException      возможная ошибка ввода-вывода
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Извлечение заголовка Authorization
        String authHeader = request.getHeader("Authorization");

        // Проверка отсутствия заголовка или некорректного формата
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // Извлечение токена без "Bearer "
        String token = authHeader.substring(7);

        // Извлечение email (username) из токена
        String email = jwtService.extractUsername(token);

        // Проверка существования email и отсутствия текущей аутентификации
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Загрузка деталей пользователя по email
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Валидация токена по пользователю
            if (jwtService.isTokenValid(token, userDetails)) {

                // Создание токена аутентификации и установка его в SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Продолжение выполнения цепочки фильтров
        filterChain.doFilter(request, response);
    }
}
