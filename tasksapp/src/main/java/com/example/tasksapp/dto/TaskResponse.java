package com.example.tasksapp.dto;

import com.example.tasksapp.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private Task.Status status;

    private Task.Priority priority;

    private LocalDate dueDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
