package com.tresvita.todo.service;

import com.tresvita.todo.model.Todo;
import com.tresvita.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Todo Service - Business logic layer for Todo operations
 * Managed by Wissen Team
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TodoService {
    
    private final TodoRepository todoRepository;
    
    /**
     * Get all todos
     */
    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        log.debug("Fetching all todos");
        return todoRepository.findAll();
    }
    
    /**
     * Get todo by ID
     */
    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        log.debug("Fetching todo with id: {}", id);
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found with id: " + id));
    }
    
    /**
     * Create a new todo
     */
    public Todo create(Todo todo) {
        log.info("Creating new todo: {}", todo.getTitle());
        todo.setId(null); // Ensure new entity
        todo.setCompleted(false); // Default to not completed
        return todoRepository.save(todo);
    }
    
    /**
     * Update an existing todo
     */
    public Todo update(Long id, Todo todoDetails) {
        log.info("Updating todo with id: {}", id);
        Todo todo = findById(id);
        
        if (todoDetails.getTitle() != null) {
            todo.setTitle(todoDetails.getTitle());
        }
        if (todoDetails.getDescription() != null) {
            todo.setDescription(todoDetails.getDescription());
        }
        todo.setCompleted(todoDetails.isCompleted());
        
        return todoRepository.save(todo);
    }
    
    /**
     * Delete a todo
     */
    public void delete(Long id) {
        log.info("Deleting todo with id: {}", id);
        Todo todo = findById(id);
        todoRepository.delete(todo);
    }
    
    /**
     * Find todos by completion status
     */
    @Transactional(readOnly = true)
    public List<Todo> findByCompleted(boolean completed) {
        log.debug("Fetching todos with completed status: {}", completed);
        return todoRepository.findByCompleted(completed);
    }
    
    /**
     * Search todos by title
     */
    @Transactional(readOnly = true)
    public List<Todo> searchByTitle(String title) {
        log.debug("Searching todos with title containing: {}", title);
        return todoRepository.findByTitleContainingIgnoreCase(title);
    }
    
    /**
     * Toggle todo completion status
     */
    public Todo toggleComplete(Long id) {
        log.info("Toggling completion status for todo id: {}", id);
        Todo todo = findById(id);
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }
}
