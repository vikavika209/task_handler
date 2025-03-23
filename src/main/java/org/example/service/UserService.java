package org.example.service;

import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервисный класс для работы с пользователями.
 * <p>
 * Реализует интерфейс {@link UserDetailsService} для интеграции с Spring Security.
 * Предоставляет методы для получения пользователей по ID и email.
 * </p>
 * <p>
 * Используется для аутентификации и авторизации пользователей.
 * </p>
 *
 * Примеры использования:
 * <pre>
 * User user = userService.getUserById(1L);
 * </pre>
 *
 */

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    Logger logger = LoggerFactory.getLogger(UserService.class);


    /**
     * Загружает пользователя по email для аутентификации.
     * <p>
     * Метод используется Spring Security в процессе аутентификации.
     * Если пользователь не найден, выбрасывается {@link UsernameNotFoundException}.
     * </p>
     *
     * @param email email пользователя
     * @return {@link UserDetails} информация о пользователе
     * @throws UsernameNotFoundException если пользователь с указанным email не найден
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));
    }

    /**
     * Получает пользователя по его уникальному идентификатору (ID).
     * <p>
     * Если пользователь не найден, выбрасывается {@link EntityNotFoundException}.
     * </p>
     *
     * @param id уникальный идентификатор пользователя
     * @return объект {@link User}, найденный по ID
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     */

    public User getUserById(Long id){
        logger.info("Метод getUserById начал работу");
        User user = userRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Пользователь не найден с ID: " + id));
        logger.info("Пользователь успешно найден: {}, {}", user.getId(), user.getEmail());
        return user;
    }

    /**
     * Получает пользователя по его email.
     * <p>
     * Если пользователь не найден, выбрасывается {@link EntityNotFoundException}.
     * </p>
     *
     * @param email email пользователя
     * @return объект {@link User}, найденный по email
     * @throws EntityNotFoundException если пользователь с указанным email не найден
     */

    public User getUserByEmail(String email){
        logger.info("Метод getUserByEmail начал работу");
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Пользователь не найден с email: " + email));
        logger.info("Пользователь успешно найден: {}, {}", user.getId(), user.getEmail());
        return user;
    }
}
