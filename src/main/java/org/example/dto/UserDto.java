package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO пользователя с основными данными")
public class UserDto {
    @Schema(description = "Уникальный идентификатор пользователя", example = "42")
    private Long id;

    @Schema(description = "Имя пользователя", example = "ivan_petrov")
    private String username;

    /**
     * Преобразует сущность User в UserDto.
     *
     * @param user сущность пользователя
     * @return DTO пользователя
     */

    public UserDto toUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        return this;
    }
}
