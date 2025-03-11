package org.example.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.example.dto.TaskDto;
import org.example.dto.UserDto;
import org.example.entity.*;
import org.example.exception.EntityNotFoundException;
import org.example.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

class TaskServiceTest {

    private final Logger logger = LoggerFactory.getLogger(TaskServiceTest.class);

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskService(taskRepository, userService);
    }

    @Test
    void createTask() {
        User author = new User(1L, "author@example.com", "password", Role.USER);
        User assignee = new User(2L, "assignee@example.com", "password", Role.USER);

        TaskDto taskDto = TaskDto.builder()
                .title("Task title")
                .status(TaskStatus.IN_PROGRESS)
                .assignee(new UserDto().toUserDto(assignee))
                .description("Task description")
                .priority(TaskPriority.HIGH)
                .build();

        String email = "author@example.com";
        String assigneeEmail = "assignee@example.com";

        Task expectedTask = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus())
                .priority(taskDto.getPriority())
                .author(author)
                .assignee(assignee)
                .build();

        when(userService.getUserByEmail(email)).thenReturn(author);
        when(userService.getUserByEmail(assigneeEmail)).thenReturn(assignee);
        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

        Task actualTask = taskService.createTask(taskDto, email, assigneeEmail);

        assertNotNull(actualTask);
        assertEquals(expectedTask.getTitle(), actualTask.getTitle());
        assertEquals(expectedTask.getDescription(), actualTask.getDescription());
        assertEquals(expectedTask.getStatus(), actualTask.getStatus());
        assertEquals(expectedTask.getAssignee().getEmail(), actualTask.getAssignee().getEmail());
        assertEquals(expectedTask.getAuthor().getId(), actualTask.getAuthor().getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTask() {
        Long taskId = 1L;

        Task expectedTask = Task.builder()
                        .id(1L)
                        .title("Task title")
                        .description("Task Description")
                        .priority(TaskPriority.MEDIUM)
                        .status(TaskStatus.PENDING)
                        .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(expectedTask));

        Task actualTask = taskService.getTask(taskId);

        assertNotNull(actualTask);
        assertEquals(expectedTask.getTitle(), actualTask.getTitle());
        assertEquals(expectedTask.getDescription(), actualTask.getDescription());
        assertEquals(expectedTask.getStatus(), actualTask.getStatus());
        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTask_ShouldThrowException_WhenTaskNotFound() {
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> taskService.getTask(taskId));
    }

    @Test
    void updateTask_ShouldUpdateTask_WhenAdmin() {

        Long taskId = 1L;

        String email = "admin@example.com";

        User adminUser = new User(1L, "admin@example.com", "password", Role.ADMIN);
        User assigneeUser = new User(2L, "assignee@example.com", "password", Role.USER);
        UserDto assigneeUserDto = new UserDto().toUserDto(assigneeUser);
        logger.info("UserDto с ID={} создан", assigneeUserDto.getId());

        TaskDto taskDto = TaskDto.builder()
                .title("Updated Title")
                .description("Updated Description")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .assignee(assigneeUserDto)
                .build();

        String newComment = "Updated comment";

        Task task = Task.builder()
                .id(1L)
                .title("Task Title")
                .description("Task Description")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .assignee(assigneeUser)
                .build();

        when(userService.getUserByEmail(email)).thenReturn(adminUser);
        when(userService.getUserById(2L)).thenReturn(assigneeUser);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto updatedTask = taskService.updateTask(taskId, taskDto, email, newComment);

        assertEquals(taskDto.getTitle(), updatedTask.getTitle());
        assertEquals(taskDto.getDescription(), updatedTask.getDescription());
        assertEquals(taskDto.getPriority(), updatedTask.getPriority());
        assertEquals(taskDto.getStatus(), updatedTask.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask() {
        Long taskId = 1L;

        Task task = Task.builder()
                .id(1L)
                .title("Task Title")
                .description("Task Description")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {

        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.deleteTask(taskId));
    }

}