package com.example.tasksapp.service;

import com.example.tasksapp.dto.TaskCreateRequest;
import com.example.tasksapp.dto.TaskResponse;
import com.example.tasksapp.dto.TaskUpdateRequest;
import com.example.tasksapp.mapper.TaskMapper;
import com.example.tasksapp.model.Task;
import com.example.tasksapp.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldGetTasksWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Task task = createTask(1L, "Test Task");
        TaskResponse taskResponse = createTaskResponse(1L, "Test Task");
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findTasks(null, null, null, pageable)).thenReturn(taskPage);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.getTasks(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findTasks(null, null, null, pageable);
    }

    @Test
    void shouldGetTasksWithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Task task = createTask(1L, "Test Task");
        task.setStatus(Task.Status.OPEN);
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findTasks(Task.Status.OPEN, null, null, pageable)).thenReturn(taskPage);
        when(taskMapper.toResponse(task)).thenReturn(createTaskResponse(1L, "Test Task"));

        Page<TaskResponse> result = taskService.getTasks(Task.Status.OPEN, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findTasks(Task.Status.OPEN, null, null, pageable);
    }

    @Test
    void shouldCreateTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("Description");
        request.setPriority(Task.Priority.HIGH);
        request.setDueDate(LocalDate.of(2025, 10, 15));

        Task task = createTask(null, "New Task");
        Task savedTask = createTask(1L, "New Task");
        TaskResponse taskResponse = createTaskResponse(1L, "New Task");

        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        verify(taskRepository).save(task);
    }

    @Test
    void shouldUpdateTask() {
        Long taskId = 1L;
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setTitle("Updated Task");
        request.setStatus(Task.Status.IN_PROGRESS);

        Task existingTask = createTask(taskId, "Original Task");
        Task updatedTask = createTask(taskId, "Updated Task");
        updatedTask.setStatus(Task.Status.IN_PROGRESS);
        TaskResponse taskResponse = createTaskResponse(taskId, "Updated Task");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(taskResponse);

        TaskResponse result = taskService.updateTask(taskId, request);

        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getTitle()).isEqualTo("Updated Task");
        verify(taskMapper).updateEntityFromRequest(existingTask, request);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTask() {
        Long taskId = 999L;
        TaskUpdateRequest request = new TaskUpdateRequest();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(taskId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 999");

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTask() {
        Long taskId = 1L;
        Task task = createTask(taskId, "Task to delete");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(taskId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Task not found with id: 999");

        verify(taskRepository, never()).delete(any());
    }

    private Task createTask(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setStatus(Task.Status.OPEN);
        return task;
    }

    private TaskResponse createTaskResponse(Long id, String title) {
        TaskResponse response = new TaskResponse();
        response.setId(id);
        response.setTitle(title);
        response.setStatus(Task.Status.OPEN);
        return response;
    }
}
