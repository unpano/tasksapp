package com.example.tasksapp.mapper;

import com.example.tasksapp.dto.TaskCreateRequest;
import com.example.tasksapp.dto.TaskResponse;
import com.example.tasksapp.dto.TaskUpdateRequest;
import com.example.tasksapp.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", defaultValue = "OPEN")
    Task toEntity(TaskCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(@MappingTarget Task task, TaskUpdateRequest request);

    TaskResponse toResponse(Task task);
}
