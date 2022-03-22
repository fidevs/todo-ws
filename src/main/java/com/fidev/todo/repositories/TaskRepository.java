package com.fidev.todo.repositories;

import java.util.List;

import com.fidev.todo.model.Task;
import com.fidev.todo.model.TaskStatus;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {

    <T> List<T> findAllByStatus(TaskStatus status, Sort sort, Class<T> type);

    <T> List<T> findAllByStatusNot(TaskStatus status, Sort sort, Class<T> type);
    
}
