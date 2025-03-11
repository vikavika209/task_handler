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

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));
    }

    public User getUserById(Long id){
        logger.info("Метод getUserById начал работу");
        User user = userRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("Пользователь не найден с ID: " + id));
        logger.info("Пользователь успешно найден: {}, {}", user.getId(), user.getEmail());
        return user;
    }

    public User getUserByEmail(String email){
        logger.info("Метод getUserByEmail начал работу");
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("Пользователь не найден с email: " + email));
        logger.info("Пользователь успешно найден: {}, {}", user.getId(), user.getEmail());
        return user;
    }
}
