package com.tresvita.todo.repository;

import com.tresvita.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Todo Repository - Data access layer for Todo entities
 * Managed by Wissen Team
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    /**
     * Find todos by completion status
     */
    List<Todo> findByCompleted(boolean completed);
    
    /**
     * Find todos by title containing search term (case insensitive)
     */
    List<Todo> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find todos by completion status ordered by creation date
     */
    List<Todo> findByCompletedOrderByCreatedAtDesc(boolean completed);
}
