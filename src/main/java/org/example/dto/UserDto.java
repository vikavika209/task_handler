package org.example.dto;

import org.example.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;

    public UserDto toUserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        return this;
    }
}
