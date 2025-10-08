package com.example.tasksapp.dto;

import com.example.tasksapp.model.Task;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Task.Status status;

    private Task.Priority priority;

    private LocalDate dueDate;
}
