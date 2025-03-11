package org.example.dto;

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
public class TaskDto {
    private long id;

    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private UserDto author;

    private UserDto assignee;

    public TaskDto toDto(Task task){
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.assignee = new UserDto().toUserDto(task.getAssignee());
        this.author = new UserDto().toUserDto(task.getAuthor());
        return this;
    }
}