package org.example.controller;

import org.example.entity.User;
import org.example.security.JwtResponse;
import org.example.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(JwtService jwtService, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(
            summary = "Аутентификация пользователя",
            description = "Аутентифицирует пользователя по его email и паролю, возвращает JWT токен.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "JWT токен успешно сгенерирован",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Неавторизован - неверные учетные данные",
                            content = @Content
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody User authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }
}
