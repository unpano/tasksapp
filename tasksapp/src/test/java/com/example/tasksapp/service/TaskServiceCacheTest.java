package com.example.tasksapp.service;

import com.example.tasksapp.dto.TaskCreateRequest;
import com.example.tasksapp.dto.TaskResponse;
import com.example.tasksapp.dto.TaskUpdateRequest;
import com.example.tasksapp.mapper.TaskMapper;
import com.example.tasksapp.model.Task;
import com.example.tasksapp.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=caffeine",
    "spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=600s"
})
class TaskServiceCacheTest {

    @Autowired
    private TaskService taskService;

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private TaskMapper taskMapper;

    @Autowired
    private CacheManager cacheManager;

    private Task task;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        cacheManager.getCacheNames().forEach(cacheName ->
            cacheManager.getCache(cacheName).clear()
        );

        // Setup test data
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.Status.OPEN);
        task.setPriority(Task.Priority.MEDIUM);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskResponse = new TaskResponse();
        taskResponse.setId(1L);
        taskResponse.setTitle("Test Task");
        taskResponse.setDescription("Test Description");
        taskResponse.setStatus(Task.Status.OPEN);
        taskResponse.setPriority(Task.Priority.MEDIUM);
        taskResponse.setCreatedAt(LocalDateTime.now());
        taskResponse.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getTaskById_shouldCacheResult() {
        // Given
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        // When - First call should hit database
        TaskResponse result1 = taskService.getTaskById(1L);

        // Then
        assertThat(result1).isNotNull();
        assertThat(result1.getId()).isEqualTo(1L);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskMapper, times(1)).toResponse(task);

        // When - Second call should use cache (no additional DB call)
        TaskResponse result2 = taskService.getTaskById(1L);

        // Then - Repository should still have been called only once
        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isEqualTo(1L);
        verify(taskRepository, times(1)).findById(1L); // Still only 1 call
        verify(taskMapper, times(1)).toResponse(task); // Still only 1 call

        // Verify cache contains the entry
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        assertThat(cache).isNotNull();
        assertThat(cache.get(1L)).isNotNull();
    }

    @Test
    void getTaskById_multipleTasks_shouldCacheIndependently() {
        // Given
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        TaskResponse taskResponse2 = new TaskResponse();
        taskResponse2.setId(2L);
        taskResponse2.setTitle("Task 2");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        when(taskMapper.toResponse(task2)).thenReturn(taskResponse2);

        // When - Fetch both tasks
        taskService.getTaskById(1L);
        taskService.getTaskById(2L);

        // Fetch them again
        taskService.getTaskById(1L);
        taskService.getTaskById(2L);

        // Then - Each should be fetched from DB only once
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).findById(2L);

        // Verify both are cached
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        assertThat(cache.get(1L)).isNotNull();
        assertThat(cache.get(2L)).isNotNull();
    }

    @Test
    void updateTask_shouldUpdateCache() {
        // Given - Task already in cache
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        taskService.getTaskById(1L); // Load into cache

        // Setup for update
        TaskUpdateRequest updateRequest = new TaskUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setStatus(Task.Status.IN_PROGRESS);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setStatus(Task.Status.IN_PROGRESS);

        TaskResponse updatedResponse = new TaskResponse();
        updatedResponse.setId(1L);
        updatedResponse.setTitle("Updated Title");
        updatedResponse.setStatus(Task.Status.IN_PROGRESS);

        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.toResponse(updatedTask)).thenReturn(updatedResponse);
        doNothing().when(taskMapper).updateEntityFromRequest(any(), any());

        // When - Update the task
        TaskResponse result = taskService.updateTask(1L, updateRequest);

        // Then - Cache should be updated with new value
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getStatus()).isEqualTo(Task.Status.IN_PROGRESS);

        // Verify cache contains updated value
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        TaskResponse cachedValue = cache.get(1L, TaskResponse.class);
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void deleteTask_shouldEvictFromCache() {
        // Given - Task in cache
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        taskService.getTaskById(1L); // Load into cache

        // Verify it's cached
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        assertThat(cache.get(1L)).isNotNull();

        // When - Delete the task
        doNothing().when(taskRepository).delete(any(Task.class));
        taskService.deleteTask(1L);

        // Then - Cache should no longer contain the entry
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void createTask_shouldNotAffectExistingCache() {
        // Given - Task 1 already in cache
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        taskService.getTaskById(1L); // Load into cache

        // Setup for creating new task
        TaskCreateRequest createRequest = new TaskCreateRequest();
        createRequest.setTitle("New Task");
        createRequest.setDescription("New Description");
        createRequest.setStatus(Task.Status.OPEN);
        createRequest.setPriority(Task.Priority.HIGH);

        Task newTask = new Task();
        newTask.setId(2L);
        newTask.setTitle("New Task");

        TaskResponse newTaskResponse = new TaskResponse();
        newTaskResponse.setId(2L);
        newTaskResponse.setTitle("New Task");

        when(taskMapper.toEntity(createRequest)).thenReturn(newTask);
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);
        when(taskMapper.toResponse(newTask)).thenReturn(newTaskResponse);

        // When - Create new task
        TaskResponse result = taskService.createTask(createRequest);

        // Then - Task 1 should still be in cache (not evicted)
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        assertThat(cache.get(1L)).isNotNull();

        // New task (ID=2) should NOT be in cache yet
        assertThat(cache.get(2L)).isNull();

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void deleteTask_shouldOnlyEvictSpecificTask() {
        // Given - Multiple tasks in cache
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        TaskResponse taskResponse2 = new TaskResponse();
        taskResponse2.setId(2L);
        taskResponse2.setTitle("Task 2");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task2));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);
        when(taskMapper.toResponse(task2)).thenReturn(taskResponse2);

        taskService.getTaskById(1L);
        taskService.getTaskById(2L);

        // Verify both cached
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("taskById");
        assertThat(cache.get(1L)).isNotNull();
        assertThat(cache.get(2L)).isNotNull();

        // When - Delete task 1
        doNothing().when(taskRepository).delete(any(Task.class));
        taskService.deleteTask(1L);

        // Then - Only task 1 should be evicted, task 2 should remain
        assertThat(cache.get(1L)).isNull();
        assertThat(cache.get(2L)).isNotNull();
    }
}
