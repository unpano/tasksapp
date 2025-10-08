package com.example.tasksapp.controller;

import com.example.tasksapp.dto.TaskCreateRequest;
import com.example.tasksapp.dto.TaskUpdateRequest;
import com.example.tasksapp.model.Task;
import com.example.tasksapp.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldGetTasksWithPagination() throws Exception {
        createSampleTasks(15);

        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldGetSecondPageOfTasks() throws Exception {
        createSampleTasks(15);

        mockMvc.perform(get("/api/tasks")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void shouldFilterTasksByStatus() throws Exception {
        createTaskWithStatus(Task.Status.OPEN);
        createTaskWithStatus(Task.Status.DONE);
        createTaskWithStatus(Task.Status.OPEN);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void shouldFilterTasksByPriority() throws Exception {
        createTaskWithPriority(Task.Priority.HIGH);
        createTaskWithPriority(Task.Priority.LOW);
        createTaskWithPriority(Task.Priority.HIGH);

        mockMvc.perform(get("/api/tasks")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].priority", everyItem(is("HIGH"))));
    }

    @Test
    void shouldSearchTasksByQuery() throws Exception {
        createTaskWithTitle("Important meeting");
        createTaskWithTitle("Shopping list");
        createTaskWithTitle("Important deadline");

        mockMvc.perform(get("/api/tasks")
                        .param("q", "Important"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("Task description");
        request.setPriority(Task.Priority.MEDIUM);
        request.setDueDate(LocalDate.of(2025, 10, 15));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("Task description"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldFailToCreateTaskWithoutTitle() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setDescription("Task description");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateTask() throws Exception {
        Task task = createTaskWithTitle("Original Title");

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setStatus(Task.Status.IN_PROGRESS);

        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void shouldPartiallyUpdateTask() throws Exception {
        Task task = createTaskWithTitle("Original Title");

        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setStatus(Task.Status.DONE);

        mockMvc.perform(patch("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Title"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentTask() throws Exception {
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated Title");

        mockMvc.perform(patch("/api/tasks/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Task task = createTaskWithTitle("Task to delete");

        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSortTasksByDueDateAscending() throws Exception {
        createTaskWithDueDate(LocalDate.of(2025, 10, 15));
        createTaskWithDueDate(LocalDate.of(2025, 10, 10));
        createTaskWithDueDate(LocalDate.of(2025, 10, 20));

        mockMvc.perform(get("/api/tasks")
                        .param("sort", "dueDate,ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dueDate").value("2025-10-10"))
                .andExpect(jsonPath("$.content[1].dueDate").value("2025-10-15"))
                .andExpect(jsonPath("$.content[2].dueDate").value("2025-10-20"));
    }

    private void createSampleTasks(int count) {
        for (int i = 0; i < count; i++) {
            Task task = new Task();
            task.setTitle("Task " + i);
            task.setDescription("Description " + i);
            task.setStatus(Task.Status.OPEN);
            task.setPriority(Task.Priority.MEDIUM);
            taskRepository.save(task);
        }
    }

    private Task createTaskWithStatus(Task.Status status) {
        Task task = new Task();
        task.setTitle("Task");
        task.setStatus(status);
        return taskRepository.save(task);
    }

    private Task createTaskWithPriority(Task.Priority priority) {
        Task task = new Task();
        task.setTitle("Task");
        task.setStatus(Task.Status.OPEN);
        task.setPriority(priority);
        return taskRepository.save(task);
    }

    private Task createTaskWithTitle(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(Task.Status.OPEN);
        return taskRepository.save(task);
    }

    private Task createTaskWithDueDate(LocalDate dueDate) {
        Task task = new Task();
        task.setTitle("Task");
        task.setStatus(Task.Status.OPEN);
        task.setDueDate(dueDate);
        return taskRepository.save(task);
    }
}
