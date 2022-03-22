package com.fidev.todo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fidev.todo.views.TaskDTO;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {
    @Autowired
    MockMvc mvc;

    @Test // Response with 400 by invalid desc
    void saveTaskWithInvalidValuesTest() throws Exception {
        mvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("", 120))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is("INVALID_DESC")))
                .andExpect(jsonPath("$.message", is("Es necesaria una descripción para la tarea")))
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
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(status().isCreated());
    }

    @Test // Can search tasks without filters, return all tasks by default
    void findTaskListWithoutFiltersTest() throws Exception {
        mvc.perform(get("/task"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test // Consult task list by each status, order by status by default
    void findTaskListOnlyByStatusTest() throws Exception {
        String[] status = new String[] { "PENDING", "COMPLETED" };

        for (String state : status) {
            mvc.perform(get("/task")
                    .param("status", state))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].status", is(state)));
        }
    }

    @Test // Consult sorted task list
    void findTaskListSortedTest() throws Exception {
        mvc.perform(get("/task")
                .param("orderBy", "desc")
                .param("orderBy", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test // Consult task list sorted by status PENDING
    void findTaskListByStatusAndFiltersAndOrderTest() throws Exception {
        mvc.perform(get("/task")
                .param("status", "PENDING")
                .param("orderBy", "desc")
                .param("orderBy", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test // Try to update task with a invalid delay value
    void updateTaskWithAnInvalidDelayValueTest() throws Exception {
        String taskID = "SOME-ID"; // TODO: Get from DB
        mvc.perform(put("/task/{id}", taskID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", -1))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is("INVALID_DELAY")))
                .andExpect(jsonPath("$.message", is("La duración estimada no puede ser menor a 1")))
                .andExpect(status().isBadRequest());
    }

    @Test // Update task by invalid ID
    void updateTaskWithAnInvalidIDTest() throws Exception {
        mvc.perform(put("/task/{id}", "f72094de-3228-4e55-9018-5280a6c341d3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", 5))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("No se encontró la tarea con el ID solicitado")))
                .andExpect(status().isNotFound());
    }

    @Test // Update completed task
    void updateCompletedTaskTest() throws Exception {
        String taskID = "SOME_ID"; // TODO: Get from DB
        mvc.perform(put("/task/{id}", taskID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO("Alguna descripción", 5))))
                .andDo(print())
                .andExpect(jsonPath("$.code", is("INVALID_ACTION")))
                .andExpect(jsonPath("$.message", is("No es posible actualizar una tarea completada")))
                .andExpect(status().isConflict());
    }

    @Test // Update task successfully
    void updateTaskSuccessfully() throws Exception {
        // TODO: Get taskID from DB
        String taskID = "SOME_ID", description = "Hacer mi tarea de física";
        float delay = 120;
        mvc.perform(put("/task/{id}", taskID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.toJson(new TaskDTO(description, delay))))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(taskID)))
                .andExpect(jsonPath("$.desc", is(description)))
                .andExpect(jsonPath("$.duration").value(delay))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(status().isOk());
    }

    @Test // Mark task as completed by invalid ID
    void markTaskAsCompletedByInvalidIDTest() throws Exception {
        mvc.perform(put("/task/{id}/status", "f72094de-3228-4e55-9018-5280a6c341d3")
                .param("delay", "120"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("No se encontró la tarea con el ID solicitado")));
    }

    @Test // Mark task as completed
    void markTaskAsCompletedSuccessfullyTest() throws Exception {
        String taskID = "SOME_ID"; // TODO: Get from db
        mvc.perform(put("/task/{id}/status", taskID)
                .param("delay", "120"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskID)))
                .andExpect(jsonPath("$.desc").exists())
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.finalDate").exists())
                .andExpect(jsonPath("$.delay", is(120)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test // Mark task as deleted by invalid ID
    void markTaskAsDeletedByInvalidIDTest() throws Exception {
        mvc.perform(put("/task/{id}/status", "f72094de-3228-4e55-9018-5280a6c341d3"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("No se encontró la tarea con el ID solicitado")));
    }

    @Test // Mark task as deleted
    void markTaskAsDeletedSuccessfullyTest() throws Exception {
        String taskID = "SOME_ID"; // TODO: Get from db
        mvc.perform(delete("/task/{id}/status", taskID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(taskID)))
                .andExpect(jsonPath("$.desc").exists())
                .andExpect(jsonPath("$.duration").exists())
                .andExpect(jsonPath("$.status", is("DELETED")));
    }

}
