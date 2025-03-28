package org.example.service;

import org.example.convertor.TaskConvertor;
import org.example.dto.TaskDto;
import org.example.entity.*;
import org.example.exception.AccessDeniedException;
import org.example.exception.EntityNotFoundException;
import org.example.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервисный класс для управления задачами (Task).
 * Предоставляет CRUD-операции и бизнес-логику по работе с задачами.
 * <p>
 * Зависимости:
 * {@link TaskRepository}, {@link UserService}, {@link TaskConvertor}.
 * </p>
 *
 */

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final UserService userService;
    private final TaskConvertor taskConvertor;

    /**
     * Создаёт новую задачу.
     *
     * @param dto          DTO объекта Task с информацией о задаче
     * @param email        email автора задачи
     * @param assigneeEmail email исполнителя задачи
     * @return сохранённая задача
     */

    @Transactional
    public Task createTask(TaskDto dto, String email, String assigneeEmail){
        logger.info("Метод createTask начал работу");

        Task task = new Task();

        try {
            User author = userService.getUserByEmail(email);
            User assignee = userService.getUserByEmail(assigneeEmail);

        task = taskConvertor.taskDtoToTask(dto);
        logger.info("Задача сохранена: {}", task.toString());

        }catch (EntityNotFoundException e){
            logger.error("Пользователь не найден: " + e.getMessage());
        }return taskRepository.save(task);
    }

    /**
     * Получает задачу по её ID.
     *
     * @param id уникальный идентификатор задачи
     * @return найденная задача
     * @throws EntityNotFoundException если задача не найдена
     */

    public Task getTask(Long id) {
        logger.info("Метод getTask начал работу");
        Task task = taskRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Задача не найдена с ID: " + id));

        logger.info("Задача с ID {} успешно найдена", id);
        return task;
    }

    /**
     * Получает список всех задач.
     *
     * @return список всех задач
     */

    public Page<Task> getTasks(Pageable pageable) {
        logger.info("Метод getTasks начал работу с пагинацией: страница {}, размер {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> tasks = taskRepository.findAll(pageable);
        logger.info("Список задач успешно найден");
        return tasks;
    }

    /**
     * Получает список задач, созданных конкретным автором.
     *
     * @param authorId ID автора задач
     * @return список задач, созданных данным пользователем
     */

    public Page<Task> getTasksByAuthor(Long authorId, Pageable pageable){
        logger.info("Метод getTasksByAuthor начал работу с пагинацией: страница {}, размер {}", pageable.getPageNumber(), pageable.getPageSize());

        User author = userService.getUserById(authorId);
        logger.info("Пользователь с ID: {} успешно найден", author.getId());

        Page<Task> tasks = taskRepository.findByAuthor(author, pageable);
        logger.info("Список задач автора с ID: {} успешно найден", authorId);
        return tasks;
    }

    /**
     * Получает список задач, назначенных конкретному исполнителю.
     *
     * @param assigneeId ID пользователя-исполнителя
     * @return список задач, назначенных пользователю
     */

    public Page<Task> getTasksByAssignee (Long assigneeId, Pageable pageable){
        logger.info("Метод getTasksByAssignee начал работу с пагинацией: страница {}, размер {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Task> tasks = Page.empty(pageable);
        try {
            User assignee = userService.getUserById(assigneeId);
            logger.info("Пользователь с ID: {} успешно найден", assignee.getId());

            tasks = taskRepository.findByAssignee(assignee, pageable);

            logger.info("Список задач исполнителя с ID: {} успешно найден", assignee.getId());
        } catch (EntityNotFoundException e) {
            logger.error("Пользователь не найден: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Обновляет задачу по её ID.
     * <p>
     * Если пользователь является ADMIN, он может изменить поля задачи и назначить исполнителя.
     * Если пользователь является USER, он может обновить только статус задачи или добавить комментарий.
     * </p>
     *
     * @param id         ID обновляемой задачи
     * @param taskDto    DTO задачи с новыми данными
     * @param email      email текущего пользователя, выполняющего обновление
     * @param newComment новый комментарий к задаче (если добавляется комментарий)
     * @return обновлённая задача в виде DTO
     * @throws EntityNotFoundException если задача или пользователь не найдены
     * @throws AccessDeniedException   если пользователь не имеет доступа к обновлению задачи
     */

    @Transactional
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
                updateTaskFieldsForAdminRole(taskDto, task);
                setAuthor(taskDto, task, currentUser);

            } else if (currentUser.getRole() == Role.USER) {
                if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUser.getId())) {
                    throw new AccessDeniedException("Пользователь с ID: " + currentUser.getId() + " не является исполнителем задачи с ID: " + id);
                }
                updateTaskFieldsForUserRole(currentUser, taskDto, task, newComment);
            }
            logger.info("Задача успешно обновлена: {}", task.toString());

        }catch (EntityNotFoundException e){
            logger.error("Пользователь не найден: " + e.getMessage());
        }
        Task savedTask = taskRepository.save(task);
        return taskConvertor.taskToDto(savedTask);
    }

    /**
     * Удаляет задачу по её ID.
     *
     * @param id ID удаляемой задачи
     */

    @Transactional
    public void deleteTask(Long id){
        logger.info("Метод deleteTask начал работу");
        Task task = getTask(id);
        taskRepository.delete(task);
        logger.info("Задача с ID: {} успешно удалена", id);
    }

    /**
     * Обновляет поля задачи для пользователя с ролью ADMIN.
     * ADMIN может изменить основные поля задачи и назначить нового исполнителя.
     *
     * @param taskDto DTO задачи с новыми данными
     * @param task    задача, которая будет обновлена
     */

    private void updateTaskFieldsForAdminRole(TaskDto taskDto, Task task){
        if (taskDto.getTitle() != null) task.setTitle(taskDto.getTitle());
        if (taskDto.getDescription() != null) task.setDescription(taskDto.getDescription());
        if (taskDto.getPriority() != null) task.setPriority(taskDto.getPriority());
        if (taskDto.getStatus() != null) task.setStatus(taskDto.getStatus());

        if (taskDto.getAssignee() != null) {
            User assignee = userService.getUserById(taskDto.getAssignee().getId());
            task.setAssignee(assignee);
        }
    }

    /**
     * Назначает автора задачи.
     * <p>
     * Если автор передан в {@link TaskDto}, используется его значение,
     * иначе текущий пользователь назначается автором, если автор ещё не задан.
     * </p>
     *
     * @param taskDto DTO задачи
     * @param task    задача, в которую назначается автор
     * @param user    текущий пользователь
     */

    private void setAuthor (TaskDto taskDto, Task task, User user){
        if (taskDto.getAuthor() != null) {
            task.setAuthor(userService.getUserById(taskDto.getAuthor().getId()));
        } else if (task.getAuthor() == null) {
            task.setAuthor(user);
        }
    }

    /**
     * Обновляет статус задачи или добавляет комментарий для пользователя с ролью USER.
     *
     * @param user       текущий пользователь (исполнитель задачи)
     * @param taskDto    DTO задачи с новым статусом (если передан)
     * @param task       задача, которая будет обновлена
     * @param newComment новый комментарий (если передан)
     */

    private void updateTaskFieldsForUserRole(User user, TaskDto taskDto, Task task, String newComment){
        if (taskDto.getStatus() != null) {
            task.setStatus(taskDto.getStatus());
        } else if (newComment != null) {

            Comment comment = Comment.builder()
                    .task(task)
                    .author(user)
                    .content(newComment)
                    .build();

            task.getComments().add(comment);
        }
    }
}
