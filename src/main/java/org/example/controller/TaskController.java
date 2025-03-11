package org.example.controller;

import org.example.dto.TaskDto;
import org.example.entity.Task;
import org.example.service.TaskService;
import org.example.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task API", description = "Управление задачами: получение, обновление задач")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @Operation(
            summary = "Получить задачи по автору",
            description = "Возвращает список задач, созданных указанным автором"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "404", description = "Автор не найден")
    })
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Task>> getTasksByAuthor(
            @Parameter(description = "ID автора задач", example = "1")
            @PathVariable Long authorId) {
        return ResponseEntity.ok(taskService.getTasksByAuthor(authorId));
    }

    @Operation(
            summary = "Получить задачи по исполнителю",
            description = "Возвращает список задач, назначенных указанному исполнителю"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "404", description = "Исполнитель не найден")
    })
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<Task>> getTasksByAssignee(
            @Parameter(description = "ID исполнителя задач", example = "2")
            @PathVariable Long assigneeId) {
        return ResponseEntity.ok(taskService.getTasksByAssignee(assigneeId));
    }

    @Operation(
            summary = "Обновить задачу",
            description = "Обновляет задачу по её ID. Доступно только авторизованным пользователям"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "403", description = "Нет прав на обновление задачи"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @Parameter(description = "ID задачи для обновления", example = "5")
            @PathVariable Long id,
            @Parameter(description = "Данные для обновления задачи")
            @RequestBody TaskDto taskDto,
            @Parameter(description = "Комментарий к обновлению", example = "Обновлено приоритетное задание")
            @RequestParam(required = false) String comment,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return ResponseEntity.ok(taskService.updateTask(id, taskDto, userEmail, comment));
    }
}
