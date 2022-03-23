package com.fidev.todo.controllers;

import java.util.List;

import com.fidev.todo.exceptions.TodoException;
import com.fidev.todo.service.TaskService;
import com.fidev.todo.util.AppConstants;
import com.fidev.todo.views.TaskDTO;
import com.fidev.todo.views.TaskDetailsDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {

    private TaskService service;

    @Autowired
    public void setTaskService(TaskService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping // Save task
    public TaskDetailsDTO saveNewTask(@RequestBody TaskDTO request) throws TodoException {
        log.info("Save new task: {}", request.getDesc());

        return service.saveNewTask(request);
    }

    @GetMapping // Search task list
    public List<TaskDetailsDTO> searchTaskList(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String orderBy,
        @RequestParam(required = false) String order
    ) {
        log.info("Search task list by status: {} and sort by: {} {}", status, orderBy, order);

        return service.searchTaskList(status, orderBy, order);
    }

    @PutMapping("/{id}") // Update task by TaskID
    public TaskDetailsDTO updateTaskByID(@PathVariable String id, @RequestBody TaskDTO update)
        throws TodoException {
        log.info("Update task with id: {}", id);

        return service.updateTaskByID(id, update);
    }

    @PutMapping("/{id}/status") // Mark task as completed
    public TaskDetailsDTO markTaskAsCompleted(@PathVariable String id, @RequestParam(required = true) Float delay)
        throws TodoException {
        log.info("Mark task with id: {} as completed in {} minutes", id, delay);

        return service.markTaskByIDAsCompleted(id, delay);
    }

    @DeleteMapping("/{id}/status") // Mark task as deleted
    public TaskDetailsDTO markTaskAsDeleted(@PathVariable String id) throws TodoException {
        log.info("Mark task with ID: {} as deleted", id);

        return service.markTaskByIDAsDeleted(id);
    }

}
