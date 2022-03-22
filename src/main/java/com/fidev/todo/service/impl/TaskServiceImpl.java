package com.fidev.todo.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fidev.todo.exceptions.TodoException;
import com.fidev.todo.mappers.TaskMapper;
import com.fidev.todo.model.Task;
import com.fidev.todo.model.TaskStatus;
import com.fidev.todo.repositories.TaskRepository;
import com.fidev.todo.service.TaskService;
import com.fidev.todo.util.AppConstants;
import com.fidev.todo.views.TaskDTO;
import com.fidev.todo.views.TaskDetailsDTO;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;

    public TaskServiceImpl(TaskRepository repository, TaskMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Save new task
     * @param request Task details to save
     * @return New task saved details
     * @throws TodoException
     */
    @Override
    public TaskDetailsDTO saveNewTask(TaskDTO request) throws TodoException {
        validateTask(request); // Validate task request
        request.setDesc(request.getDesc().trim());
        log.info("Task details are valid");

        Task newTask = new Task(request.getDesc(), request.getDuration());
        newTask = this.repository.save(newTask); // Save task

        return mapper.mapTaskToDetails(newTask); // Return task details
    }

    /**
     * Consult task list
     * @param status Status to filter tasks
     * @param orderBy Field name to sort list
     * @param order Direction to sort list
     */
    @Override
    public List<TaskDetailsDTO> searchTaskList(String status, String orderBy, String order) {
        List<Task> tasks;

        // Obtain field name to sort list, sort by status by default
        String fieldName = AppConstants.PARAMS.getOrDefault(orderBy, "status");
        // Obtaint sort direction, sort by DESC direction by default
        Direction direction = (order != null && order.equalsIgnoreCase("ASC"))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        TaskStatus taskStatus = null;
        try { // Obtain task status to filter query
            taskStatus = TaskStatus.valueOf(status);
        } catch (Exception e) {
            log.warn("Status param is invalid: [{}]. Task list search without status filter", status);
            e.printStackTrace();
        }

        // Search task list
        tasks = (taskStatus != null && !taskStatus.equals(TaskStatus.DELETED)) // If status param is invalid, ignore it
            ? repository.findAllByStatus(taskStatus, Sort.by(direction, fieldName), Task.class)
            // Not return task if is marked as deleted
            : repository.findAllByStatusNot(TaskStatus.DELETED, Sort.by(direction, fieldName), Task.class);

        log.info("Found {} tasks with status: {} and order by {} {}", tasks.size(), taskStatus, fieldName, direction);
        return tasks.stream().map(mapper::mapTaskToDetails).collect(Collectors.toList());
    }

    /**
     * Update task details by task ID
     * @param id Task ID
     * @param update Task details to update
     * @return Task updated
     * @throws TodoException
     */
    @Override
    public TaskDetailsDTO updateTaskByID(String id, TaskDTO update) throws TodoException {
        validateTask(update);
        update.setDesc(update.getDesc().trim());

        Task task = searchTaskByID(id);
        if (task.getStatus().equals(TaskStatus.COMPLETED)) { // Can't update a completed task
            log.error("Can't update task with ID: {}. It is completed", task.getId());
            throw new TodoException(
                HttpStatus.CONFLICT,
                AppConstants.INVALID_ACTION_CODE,
                AppConstants.INVALID_ACTION_MESSAGE
            );
        }

        // Update task details
        task.setDescription(update.getDesc());
        task.setDuration(update.getDuration());

        Task taskUpdated = repository.save(task); // Save changes in DB
        log.info("Task with ID: {} updated successfully", task.getId());

        return mapper.mapTaskToDetails(taskUpdated);
    }

    /**
     * Mark task as completed by task ID
     * @param id Task ID
     * @param delay Task time delay
     * @return Task marked as completed
     * @throws TodoException
     */
    @Override
    public TaskDetailsDTO markTaskByIDAsCompleted(String id, float delay) throws TodoException {
        Task task = searchTaskByID(id); // Search task

        // Update status and delay of task and set date finish
        task.setStatus(TaskStatus.COMPLETED);
        task.setDelay(delay);
        task.setFinDate(new Date());

        Task taskCompleted = repository.save(task); // Update task in DB
        log.info("Task marked as completed successfully");
        return mapper.mapTaskToDetails(taskCompleted);
    }

    /**
     * Mark task as deleted by task ID
     * @param id Task ID
     * @return Task marked as deleted
     * @throws TodoException
     */
    @Override
    public TaskDetailsDTO markTaskByIDAsDeleted(String id) throws TodoException {
        Task task = searchTaskByID(id); // Search task

        task.setStatus(TaskStatus.DELETED); //Mark task as deleted
        Task taskDeleted = repository.save(task);
        log.info("Task marked as deleted successfully");

        return mapper.mapTaskToDetails(taskDeleted);
    }

    /**
     * Search optional task by ID
     * @param id Task ID
     * @return Task entity
     * @throws TodoException
     */
    private Task searchTaskByID(String id) throws TodoException {
        Optional<Task> optTask = repository.findById(id);
        if (!optTask.isPresent()) {
            log.error("Not found any task by ID: {}", id);
            throw new TodoException(
                AppConstants.NOT_FOUND_MESSAGE,
                AppConstants.NOT_FOUND_CODE,
                HttpStatus.NOT_FOUND
            );
        }

        return optTask.get();
    }

    /**
     * Validate task request
     * @param task Task details
     * @throws TodoException
     */
    private void validateTask(TaskDTO task) throws TodoException {
        if (task.getDesc() == null || task.getDesc().trim().isEmpty() || task.getDesc().trim().length() > 100) {
            log.error("Invalid task description: [{}]", task.getDesc());
            throw new TodoException(
                AppConstants.INVALID_DESC_MESSAGE,
                AppConstants.INVALID_DESC_CODE,
                HttpStatus.BAD_REQUEST
            );
        }

        if (task.getDuration() < 1) {
            log.error("Invalid task duration: [{}]", task.getDuration());
            throw new TodoException(
                AppConstants.INVALID_DELAY_MESSAGE,
                AppConstants.INVALID_DELAY_CODE,
                HttpStatus.BAD_REQUEST
            );
        }
    }

}
