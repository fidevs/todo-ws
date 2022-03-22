package com.fidev.todo.mappers;

import com.fidev.todo.model.Task;
import com.fidev.todo.views.TaskDetailsDTO;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDetailsDTO mapTaskToDetails(Task task) {
        return new TaskDetailsDTO(
                task.getId(),
                task.getDescription(),
                task.getDuration(),
                task.getFinDate() == null ? null : task.getFinDate().toString(),
                0,
                task.getStatus().toString()
        );
    }

}
