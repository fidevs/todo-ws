package com.fidev.todo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fidev.todo.model.Task;
import com.fidev.todo.model.TaskStatus;
import com.fidev.todo.projections.TaskID;
import com.fidev.todo.repositories.TaskRepository;
import com.fidev.todo.util.AppConstants;
import com.fidev.todo.views.TaskDTO;
import com.fidev.todo.views.TaskDetailsDTO;

@Slf4j
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permit @BeforeAll on a non static method
class TaskControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private TaskRepository repository;

    @BeforeAll
    public void setup() {
        final Date theDate = new Date();
        List<Task> tasks = new ArrayList<>(); // Task list to save in DB for test
        tasks.add(new Task("A", 20, new Date(theDate.getTime() + 10), 30, TaskStatus.COMPLETED));
        tasks.add(new Task("B", 25, new Date(theDate.getTime() + 30000), 20, TaskStatus.COMPLETED));
        tasks.add(new Task("C", 50, null, 0, TaskStatus.PENDING));
        tasks.add(new Task("D", 55, new Date(theDate.getTime() + 2000), 57, TaskStatus.COMPLETED));
        tasks.add(new Task("E", 78, null, 0, TaskStatus.PENDING));
        tasks.add(new Task("F", 100, null, 0, TaskStatus.PENDING));
        tasks.add(new Task("G", 102, new Date(theDate.getTime() + 1000), 98, TaskStatus.COMPLETED));
        tasks.add(new Task("H", 120, new Date(theDate.getTime() + 50), 115, TaskStatus.DELETED));
        tasks.add(new Task("I", 130, null, 0, TaskStatus.DELETED));
        repository.saveAll(tasks);
    }

    @Test // Response with 400 by invalid desc
    void saveTaskWithInvalidValuesTest() throws Exception {
        mvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("", 120))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is(AppConstants.INVALID_DESC_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.INVALID_DESC_MESSAGE)))
                .andExpect(status().isBadRequest());
    }

    @Test // Success task created
    void saveTaskSuccessfullyTest() throws Exception {
        String taskDescription = "Hacer mi tarea";
        float taskDuration = 120;
        mvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO(taskDescription, taskDuration))))
                .andDo(print())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.desc", is(taskDescription)))
                .andExpect(jsonPath("$.duration").value(taskDuration))
                .andExpect(jsonPath("$.status", is(AppConstants.PENDING_STATUS)))
                .andExpect(status().isCreated());
    }

    @Test // Can search tasks without filters, return all tasks by default
    void findTaskListWithoutFiltersTest() throws Exception {
        String json = mvc.perform(get("/task"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        List<TaskDetailsDTO> list = JsonUtil.fromJson(json);
        // Return completed and pending task (7)
        assertThat(list).hasSize(7);
        // Verify that it does not return deleted task
        assertThat(list.stream().anyMatch(task -> task.getStatus().equals(TaskStatus.DELETED.toString()))).isFalse();
    }

    @Test // Consult task list by each status, order by status by default
    void findTaskListOnlyByStatusTest() throws Exception {
        String[] status = new String[] { AppConstants.PENDING_STATUS, AppConstants.COMPLETED_STATUS };

        for (String state : status) {
            String json = mvc.perform(get("/task")
                    .param("status", state))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andReturn().getResponse().getContentAsString();

            List<TaskDetailsDTO> list = JsonUtil.fromJson(json); // Just return the requested state
            assertThat(list.stream().anyMatch(task -> !task.getStatus().equals(state))).isFalse();
        }
    }

    @Test // Consult sorted task list
    void findTaskListSortedTest() throws Exception {
        String json = mvc.perform(get("/task")
                .param("orderBy", "desc")
                .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();
        List<TaskDetailsDTO> list = JsonUtil.fromJson(json);
        assertThat(list.get(0).getDesc()).isEqualTo("G"); // First task in order by description DESC
        assertThat(list.get(list.size()-1).getDesc()).isEqualTo("A"); // Last task in order by description DESC

        json = mvc.perform(get("/task")
                .param("orderBy", "date")
                .param("order", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();
        list = JsonUtil.fromJson(json);
        assertThat(list.get(0).getFinalDate()).isNull(); // First task in order by date ASC is a pending task (date null)
        assertThat(list.get(list.size()-1).getDesc()).isEqualTo("B"); // Last task in order by date ASC is task B
    }

    @Test // Consult task list sorted by status PENDING
    void findTaskListByStatusAndFiltersAndOrderTest() throws Exception {
        String json = mvc.perform(get("/task")
                .param("status", AppConstants.PENDING_STATUS)
                .param("orderBy", "duration")
                .param("order", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn().getResponse().getContentAsString();

        List<TaskDetailsDTO> list = JsonUtil.fromJson(json);
        assertThat(list.get(0).getDesc()).isEqualTo("F"); // First pending task task sorted by duration DESC
        assertThat(list.get(list.size()-1).getDesc()).isEqualTo("C"); // Last pending task sorted by duration DESC
    }

    @Test // Try to update task with a invalid delay value
    void updateTaskWithAnInvalidDurationValueTest() throws Exception {
        // Get ID of first pending task
        TaskID task = repository.findFirstByStatus(TaskStatus.PENDING, TaskID.class);
        mvc.perform(put("/task/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", -1))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is(AppConstants.INVALID_DURATION_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.INVALID_DURATION_MESSAGE)))
                .andExpect(status().isBadRequest());
    }

    @Test // Update task by invalid ID
    void updateTaskWithAnInvalidIDTest() throws Exception {
        mvc.perform(put("/task/{id}", "f72094de-3228-4e55-9018-5280a6c341d3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", 5))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is(AppConstants.NOT_FOUND_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.NOT_FOUND_MESSAGE)))
                .andExpect(status().isNotFound());
    }

    @Test // Update completed task
    void updateCompletedTaskTest() throws Exception {
        // Get ID of first completed task
        TaskID task = repository.findFirstByStatus(TaskStatus.COMPLETED, TaskID.class);
        mvc.perform(put("/task/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", 5))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is(AppConstants.INVALID_ACTION_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.INVALID_ACTION_MESSAGE)))
                .andExpect(status().isConflict());
    }

    @Test // Update task successfully
    void updateTaskSuccessfully() throws Exception {
        // Get ID of first pending task
        TaskID task = repository.findFirstByStatus(TaskStatus.PENDING, TaskID.class);
        String description = "Hacer mi tarea de física";
        float delay = 120;
        mvc.perform(put("/task/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO(description, delay))))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(task.getId())))
                .andExpect(jsonPath("$.desc", is(description)))
                .andExpect(jsonPath("$.duration").value(delay))
                .andExpect(jsonPath("$.status", is(AppConstants.PENDING_STATUS)))
                .andExpect(status().isOk());
    }

    @Test // Mark task as completed without delay param
    void markTaskAsCompletedWithoutDelayParamTest() throws Exception {
        // Get ID of first pending task
        TaskID task = repository.findFirstByStatus(TaskStatus.PENDING, TaskID.class);
        mvc.perform(put("/task/{id}/status", task.getId())).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test // Mark task as completed by invalid ID
    void markTaskAsCompletedByInvalidIDTest() throws Exception {
        mvc.perform(put("/task/{id}/status", "f72094de-3228-4e55-9018-5280a6c341d3")
                .param("delay", "120"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(AppConstants.NOT_FOUND_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.NOT_FOUND_MESSAGE)));
    }

    @Test // Mark task as completed
    void markTaskAsCompletedSuccessfullyTest() throws Exception {
        // Get ID of first pending task
        TaskID task = repository.findFirstByStatus(TaskStatus.PENDING, TaskID.class);
        mvc.perform(put("/task/{id}/status", task.getId())
                .param("delay", "120"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(task.getId())))
                .andExpect(jsonPath("$.desc").exists())
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.finalDate").exists())
                .andExpect(jsonPath("$.delay").value(120))
                .andExpect(jsonPath("$.status", is(AppConstants.COMPLETED_STATUS)));
    }

    @Test // Mark task as deleted by invalid ID
    void markTaskAsDeletedByInvalidIDTest() throws Exception {
        mvc.perform(delete("/task/{id}/status", "f72094de-3228-4e55-9018-5280a6c341d3"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is(AppConstants.NOT_FOUND_CODE)))
                .andExpect(jsonPath("$.message", is(AppConstants.NOT_FOUND_MESSAGE)));
    }

    @Test // Mark task as deleted
    void markTaskAsDeletedSuccessfullyTest() throws Exception {
        // Get ID of first completed task
        TaskID task = repository.findFirstByStatus(TaskStatus.COMPLETED, TaskID.class);
        mvc.perform(delete("/task/{id}/status", task.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(task.getId())))
                .andExpect(jsonPath("$.status", is(AppConstants.DELETED_STATUS)));
    }

}
