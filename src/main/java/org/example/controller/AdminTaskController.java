package org.example.controller;

import org.example.dto.TaskDto;
import org.example.entity.Task;
import org.example.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "Admin Task API", description = "Администрирование задач (CRUD) доступно только для пользователей с ролью ADMIN")
public class AdminTaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Получить список всех задач",
            description = "Доступно только для пользователей с ролью ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Список задач успешно получен")
    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает задачу по указанному идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "ID задачи", example = "1")
            @PathVariable Long id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    @Operation(
            summary = "Создать новую задачу",
            description = "Создает задачу с указанием исполнителя и автором из токена аутентификации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Parameter(description = "Данные задачи для создания")
            @RequestBody @Valid TaskDto taskDto,
            Authentication authentication,
            @Parameter(description = "Email исполнителя задачи", example = "user@example.com")
            @RequestParam String assigneeEmail) {

        String authorEmail = authentication.getName();
        Task createdTask = taskService.createTask(taskDto, authorEmail, assigneeEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @Operation(
            summary = "Обновить задачу по ID",
            description = "Позволяет обновить данные задачи и добавить комментарий"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "403", description = "Нет прав на обновление задачи")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @Parameter(description = "ID задачи для обновления", example = "5")
            @PathVariable Long id,
            @Parameter(description = "Обновленные данные задачи")
            @RequestBody TaskDto taskDto,
            Authentication authentication,
            @Parameter(description = "Новый комментарий к задаче", example = "Изменен статус на выполнено")
            @RequestParam(required = false) String newComment) {

        String userEmail = authentication.getName();
        TaskDto updatedTask = taskService.updateTask(id, taskDto, userEmail, newComment);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(
            summary = "Удалить задачу по ID",
            description = "Удаляет задачу с указанным идентификатором"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID задачи для удаления", example = "3")
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
