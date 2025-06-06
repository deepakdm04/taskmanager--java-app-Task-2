package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskExecution;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public Task createOrUpdateTask(Task task) {
        if (isCommandUnsafe(task.getCommand())) {
            throw new IllegalArgumentException("Unsafe command detected");
        }
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }

    public List<Task> findByName(String name) {
        return taskRepository.findByNameContainingIgnoreCase(name);
    }

    public TaskExecution executeTask(String taskId) throws Exception {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Date startTime = new Date();

        // Prepare command array based on OS
        String[] command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            command = new String[]{"cmd.exe", "/c", task.getCommand()};
        } else {
            command = new String[]{"/bin/sh", "-c", task.getCommand()};
        }

        Process process = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        StringBuilder outputBuilder = new StringBuilder();
        String line;

        // Read standard output
        while ((line = stdInput.readLine()) != null) {
            outputBuilder.append(line).append("\n");
        }
        // Read error output
        while ((line = stdError.readLine()) != null) {
            outputBuilder.append("[ERROR] ").append(line).append("\n");
        }

        process.waitFor();

        Date endTime = new Date();

        TaskExecution execution = new TaskExecution(startTime, endTime, outputBuilder.toString().trim());

        if (task.getTaskExecutions() == null) {
            task.setTaskExecutions(new java.util.ArrayList<>());
        }
        task.getTaskExecutions().add(execution);
        taskRepository.save(task);

        return execution;
    }

    private boolean isCommandUnsafe(String command) {
        String[] unsafe = {"rm", "shutdown", "reboot", "mkfs", "dd", ">", "<", "|", ";", "&", "`"};
        for (String word : unsafe) {
            if (command.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
