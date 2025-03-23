package org.example.security;

import org.example.service.JwtService;
import org.example.service.UserService;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация безопасности приложения.
 * <p>
 * Настраивает правила авторизации, CORS, фильтрацию JWT и менеджеры аутентификации.
 * Интегрируется с {@link UserService} для проверки пользователей.
 * </p>
 *
 * @author ...
 * @version 1.0
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Сервис для работы с пользователями, используется для аутентификации.
     */

    private final UserService userService;

    /**
     * Конфигурация аутентификации для получения {@link AuthenticationManager}.
     */

    private AuthenticationConfiguration authenticationConfiguration;

    /**
     * Основной конструктор конфигурации безопасности.
     *
     * @param userService                сервис пользователей
     * @param authenticationConfiguration конфигурация аутентификации
     */

    @Autowired
    public SecurityConfig(UserService userService, AuthenticationConfiguration authenticationConfiguration) {
        this.userService = userService;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * Альтернативный конструктор (необязательно, если не используешь его явно).
     *
     * @param userService сервис пользователей
     */

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    /**
     * Конфигурирует цепочку фильтров безопасности приложения.
     * <p>
     * Настраивает доступы к эндпоинтам, управление сессиями,
     * CSRF, а также добавляет фильтр JWT авторизации.
     * </p>
     *
     * @param http                     объект {@link HttpSecurity}
     * @param jwtAuthFilter            фильтр авторизации JWT
     * @param authenticationProvider   провайдер аутентификации
     * @return настроенный {@link SecurityFilterChain}
     * @throws Exception при ошибке конфигурации
     */

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthFilter jwtAuthFilter,
                                            AuthenticationProvider authenticationProvider) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/index.html"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/tasks/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Бин менеджера аутентификации.
     * Используется для выполнения аутентификации пользователей.
     *
     * @return {@link AuthenticationManager}
     * @throws Exception при ошибке конфигурации
     */

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Провайдер аутентификации, на основе {@link DaoAuthenticationProvider}.
     * <p>
     * Настраивает сервис пользователей и кодировщик паролей.
     * </p>
     *
     * @return {@link AuthenticationProvider}
     */

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Фильтр авторизации JWT.
     *
     * @param authenticationManager {@link AuthenticationManager}
     * @param jwtService сервис для работы с JWT токенами
     * @return {@link JwtAuthFilter}
     */

    @Bean
    public JwtAuthFilter jwtAuthFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        return new JwtAuthFilter(jwtService, userService, authenticationManager);
    }

    /**
     * Кодировщик паролей.
     * <p>
     * Используется для хэширования паролей пользователей.
     * </p>
     *
     * @return {@link PasswordEncoder} на основе {@link BCryptPasswordEncoder}
     */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Сервис работы с JWT токенами.
     *
     * @return новый экземпляр {@link JwtService}
     */

    @Bean
    public JwtService jwtService() {
        return new JwtService();
    }

    /**
     * Источник конфигурации CORS.
     * <p>
     * Настраивает разрешенные источники, методы и заголовки.
     * </p>
     *
     * @return {@link CorsConfigurationSource}
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://your-production-site.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

