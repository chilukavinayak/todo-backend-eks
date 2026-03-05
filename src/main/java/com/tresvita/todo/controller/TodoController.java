package com.tresvita.todo.controller;

import com.tresvita.todo.model.Todo;
import com.tresvita.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Todo REST Controller - Exposes REST APIs for Todo operations
 * Managed by Wissen Team
 * 
 * Base Path: /api/todos
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class TodoController {
    
    private final TodoService todoService;
    
    /**
     * Get all todos
     * GET /api/todos
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        log.debug("GET /api/todos - Fetching all todos");
        return ResponseEntity.ok(todoService.findAll());
    }
    
    /**
     * Get todo by ID
     * GET /api/todos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        log.debug("GET /api/todos/{} - Fetching todo by id", id);
        return ResponseEntity.ok(todoService.findById(id));
    }
    
    /**
     * Create a new todo
     * POST /api/todos
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@RequestBody Todo todo) {
        log.info("POST /api/todos - Creating new todo: {}", todo.getTitle());
        Todo createdTodo = todoService.create(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTodo);
    }
    
    /**
     * Update an existing todo
     * PUT /api/todos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        log.info("PUT /api/todos/{} - Updating todo", id);
        return ResponseEntity.ok(todoService.update(id, todo));
    }
    
    /**
     * Delete a todo
     * DELETE /api/todos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        log.info("DELETE /api/todos/{} - Deleting todo", id);
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get todos by completion status
     * GET /api/todos/completed/{completed}
     */
    @GetMapping("/completed/{completed}")
    public ResponseEntity<List<Todo>> getTodosByCompleted(@PathVariable boolean completed) {
        log.debug("GET /api/todos/completed/{} - Fetching todos by status", completed);
        return ResponseEntity.ok(todoService.findByCompleted(completed));
    }
    
    /**
     * Search todos by title
     * GET /api/todos/search?title={title}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodos(@RequestParam String title) {
        log.debug("GET /api/todos/search?title={} - Searching todos", title);
        return ResponseEntity.ok(todoService.searchByTitle(title));
    }
    
    /**
     * Toggle todo completion status
     * PATCH /api/todos/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoComplete(@PathVariable Long id) {
        log.info("PATCH /api/todos/{}/toggle - Toggling todo status", id);
        return ResponseEntity.ok(todoService.toggleComplete(id));
    }
    
    /**
     * Health check endpoint
     * GET /api/todos/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "todo-backend");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
