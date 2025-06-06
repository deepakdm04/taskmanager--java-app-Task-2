package com.example.taskmanager.controller;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskExecution;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ✅ GET all tasks or find by name using ?name=xxx
    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String name) {
        if (name != null) {
            List<Task> tasks = taskService.findByName(name);
            if (tasks.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tasks);
        }
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // ✅ GET task by ID
@GetMapping("/{id}")
public ResponseEntity<Task> getTaskById(@PathVariable String id) {
    return taskService.getTaskById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}

    // ✅ PUT (create or update) a task
    @PutMapping
    public ResponseEntity<?> createOrUpdateTask(@RequestBody Task task) {
        try {
            Task savedTask = taskService.createOrUpdateTask(task);
            return ResponseEntity.ok(savedTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ DELETE a task by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ PUT: execute command for a task by ID
@PutMapping("/execute/{id}")
public ResponseEntity<?> executeTask(@PathVariable String id) {
    try {
        TaskExecution execution = taskService.executeTask(id);
        return ResponseEntity.ok(execution);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Failed to execute task: " + e.getMessage());
    }
}

}
