package com.fidev.todo.repositories;

import com.fidev.todo.model.Task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
    
}
