package org.example.repository;

import org.example.entity.Task;
import org.example.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByAuthor(User author, Pageable pageable);
    Page<Task> findByAssignee(User assignee, Pageable pageable);
}
