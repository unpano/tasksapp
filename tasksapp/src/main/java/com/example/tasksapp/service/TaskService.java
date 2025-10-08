package com.example.tasksapp.service;

import com.example.tasksapp.dto.TaskCreateRequest;
import com.example.tasksapp.dto.TaskResponse;
import com.example.tasksapp.dto.TaskUpdateRequest;
import com.example.tasksapp.mapper.TaskMapper;
import com.example.tasksapp.model.Task;
import com.example.tasksapp.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public Page<TaskResponse> getTasks(Task.Status status, Task.Priority priority, String q, Pageable pageable) {
        Page<Task> tasks = taskRepository.findTasks(status, priority, q, pageable);
        return tasks.map(taskMapper::toResponse);
    }

    @Cacheable(value = "taskById", key = "#id")
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponse(savedTask);
    }

    @Transactional
    @CachePut(value = "taskById", key = "#id")
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        taskMapper.updateEntityFromRequest(task, request);
        Task updatedTask = taskRepository.save(task);
        return taskMapper.toResponse(updatedTask);
    }

    @Transactional
    @CacheEvict(value = "taskById", key = "#id")
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        taskRepository.delete(task);
    }
}
