package org.example.service;

import org.example.dto.TaskDto;
import org.example.entity.*;
import org.example.exception.AccessDeniedException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final UserService userService;

    public Task createTask(TaskDto dto, String email, String assigneeEmail){
        logger.info("Метод createTask начал работу");

        Task task = new Task();

        try {
            User author = userService.getUserByEmail(email);
            User assignee = userService.getUserByEmail(assigneeEmail);

         task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .author(author)
                .assignee(assignee)
                .build();

        logger.info("Задача сохранена: {}", task.toString());

        }catch (EntityNotFoundException e){
            logger.error("Пользователь не найден: " + e.getMessage());
        }return taskRepository.save(task);
    }

    public Task getTask(Long id) {
        logger.info("Метод getTask начал работу");
        Task task = taskRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Задача не найдена с ID: " + id));

        logger.info("Задача с ID {} успешно найдена", id);
        return task;
    }

    public List<Task> getTasks() {
        logger.info("Метод getTasks начал работу");

        List<Task> tasks = taskRepository.findAll();
        logger.info("Список задач успешно найден");
        return tasks;
    }

    public List<Task> getTasksByAuthor(Long authorId){
        logger.info("Метод getTasksByAuthor начал работу");

        User author = userService.getUserById(authorId);
        logger.info("Пользователь с ID: {} успешно найден", author.getId());

        List<Task> tasks = taskRepository.findByAuthor(author);
        logger.info("Список задач автора с ID: {} успешно найден", authorId);
        return tasks;
    }

    public List<Task> getTasksByAssignee (Long assigneeId){
        logger.info("Метод getTasksByAssignee начал работу");
        List<Task> tasks = new LinkedList<>();

        try {
            User assignee = userService.getUserById(assigneeId);
            logger.info("Пользователь с ID: {} успешно найден", assignee.getId());

            tasks = taskRepository.findByAssignee(assignee);

            logger.info("Список задач исполнителя с ID: {} успешно найден", assignee.getId());
        }catch (EntityNotFoundException e){
            logger.error("Пользователь не найден: " + e.getMessage());
        }
        return tasks;
    }

    public TaskDto updateTask(Long id, TaskDto taskDto, String email, String newComment){
        logger.info("Метод updateTask начал работу");
        Task task = new Task();

        try {
            User currentUser = userService.getUserByEmail(email);
            logger.info("Получен пользователь с email: {}", currentUser.getEmail());

            task = taskRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Задача с ID: " + id + " не найдена."));
            logger.info("Найдена задача с ID: {}", task.getId());

            if (currentUser.getRole() == Role.ADMIN) {
                if (taskDto.getTitle() != null) task.setTitle(taskDto.getTitle());
                if (taskDto.getDescription() != null) task.setDescription(taskDto.getDescription());
                if (taskDto.getPriority() != null) task.setPriority(taskDto.getPriority());
                if (taskDto.getStatus() != null) task.setStatus(taskDto.getStatus());

                if (taskDto.getAssignee() != null) {
                    User assignee = userService.getUserById(taskDto.getAssignee().getId());
                    task.setAssignee(assignee);
                }

                if (taskDto.getAuthor() != null) {
                    task.setAuthor(userService.getUserById(taskDto.getAuthor().getId()));
                } else if (task.getAuthor() == null) {
                    task.setAuthor(currentUser);
                }

            } else if (currentUser.getRole() == Role.USER) {
                if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId())) {
                    throw new AccessDeniedException("Пользователь с ID: " + currentUser.getId() + " не является исполнителем задачи с ID: " + id);
                }
                if (taskDto.getStatus() != null) {
                    task.setStatus(taskDto.getStatus());
                } else if (newComment != null) {

                    Comment comment = Comment.builder()
                            .task(task)
                            .author(currentUser)
                            .content(newComment)
                            .build();

                    task.getComments().add(comment);
                }
            }
            logger.info("Задача успешно обновлена: {}", task.toString());

        }catch (EntityNotFoundException e){
            logger.error("Пользователь не найден: " + e.getMessage());
        }
        Task savedTask = taskRepository.save(task);
        return new TaskDto().toDto(savedTask);
    }

    public void deleteTask(Long id){
        logger.info("Метод deleteTask начал работу");
        Task task = getTask(id);
        taskRepository.delete(task);
        logger.info("Задача с ID: {} успешно удалена", id);
    }
}
