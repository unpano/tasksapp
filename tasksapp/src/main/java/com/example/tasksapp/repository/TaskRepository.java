package com.example.tasksapp.repository;

import com.example.tasksapp.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:q IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Task> findTasks(
            @Param("status") Task.Status status,
            @Param("priority") Task.Priority priority,
            @Param("q") String q,
            Pageable pageable
    );
}
