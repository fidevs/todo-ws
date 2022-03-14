package com.fidev.todo.controllers;

import com.fidev.todo.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
public class TaskController {

    private TaskService service;

    @Autowired
    public void setTaskService(TaskService service) {
        this.service = service;
    }
}
