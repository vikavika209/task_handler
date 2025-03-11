package org.example.repository;

import org.example.entity.Task;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAuthor(User author);
    List<Task> findByAssignee(User assignee);
}
