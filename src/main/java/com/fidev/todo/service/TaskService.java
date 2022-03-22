package com.fidev.todo.service;

import java.util.List;

import com.fidev.todo.exceptions.TodoException;
import com.fidev.todo.views.TaskDTO;
import com.fidev.todo.views.TaskDetailsDTO;

public interface TaskService {

    TaskDetailsDTO saveNewTask(TaskDTO request) throws TodoException;

    List<TaskDetailsDTO> searchTaskList(String status, String orderBy, String order);

    TaskDetailsDTO updateTaskByID(String id, TaskDTO update) throws TodoException;

    TaskDetailsDTO markTaskByIDAsCompleted(String id, float delay) throws TodoException;

    TaskDetailsDTO markTaskByIDAsDeleted(String id) throws TodoException;

}
