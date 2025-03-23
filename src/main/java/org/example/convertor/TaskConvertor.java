package org.example.convertor;

import org.example.dto.TaskDto;
import org.example.entity.Task;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Компонент для конвертации между сущностью {@link Task} и DTO {@link TaskDto}.
 * <p>
 * Использует {@link ModelMapper} для автоматического маппинга между объектами.
 * </p>
 *
 * Пример использования:
 * <pre>{@code
 * TaskDto dto = taskConvertor.taskToDto(task);
 * Task task = taskConvertor.taskDtoToTask(dto);
 * }</pre>
 *
 */

@Component
public class TaskConvertor {

    /**
     * Маппер для преобразования между сущностью и DTO.
     */
    private final ModelMapper modelMapper;

    /**
     * Конструктор, инициализирующий {@link ModelMapper}.
     */
    public TaskConvertor() {
        this.modelMapper = new ModelMapper();
    }

    /**
     * Преобразует {@link Task} в {@link TaskDto}.
     *
     * @param task сущность задачи
     * @return DTO задачи
     */
    public TaskDto taskToDto(Task task) {
        return modelMapper.map(task, TaskDto.class);
    }

    /**
     * Преобразует {@link TaskDto} в {@link Task}.
     *
     * @param dto DTO задачи
     * @return сущность задачи
     */
    public Task taskDtoToTask(TaskDto dto) {
        return modelMapper.map(dto, Task.class);
    }
}
