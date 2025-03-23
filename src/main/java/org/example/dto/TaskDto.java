package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.entity.Task;
import org.example.entity.TaskPriority;
import org.example.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO задачи с полями заголовка, описания, статуса, приоритета и информацией об авторе и исполнителе")
public class TaskDto {

    @Schema(description = "Уникальный идентификатор задачи", example = "1")
    private long id;

    @Schema(description = "Заголовок задачи", example = "Реализовать функционал авторизации")
    private String title;

    @Schema(description = "Подробное описание задачи", example = "Нужно добавить аутентификацию с помощью JWT")
    private String description;

    @Schema(description = "Статус задачи", example = "IN_PROGRESS")
    private TaskStatus status;

    @Schema(description = "Приоритет задачи", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Автор задачи")
    private UserDto author;

    @Schema(description = "Исполнитель задачи")
    private UserDto assignee;
}