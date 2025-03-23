package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Комментарий к задаче")
public class CommentDto {
    @Schema(description = "Уникальный идентификатор комментария", example = "1")
    private Long id;

    @Schema(description = "Текст комментария", example = "Отличная работа, продолжайте в том же духе!")
    private String content;

    @Schema(description = "Email автора комментария", example = "user@example.com")
    private String authorEmail;

    @Schema(description = "Дата и время создания комментария", example = "2024-03-23T15:30:00")
    private LocalDateTime createdAt;
}