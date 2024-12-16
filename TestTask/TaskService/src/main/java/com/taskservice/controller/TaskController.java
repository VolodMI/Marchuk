package com.taskservice.controller;

import com.taskservice.model.Task;
import com.taskservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    @Value("${file.upload-dir}")
    private static String UPLOAD_DIR;

    public TaskController(TaskRepository taskRepository) {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("username") String username,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setUsername(username);

        return getTaskResponseEntity(file, task);
    }

    private ResponseEntity<Task> getTaskResponseEntity(@RequestParam(value = "file", required = false) MultipartFile file, Task task) throws IOException {
        if (file != null && !file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.copy(file.getInputStream(), filePath);
            task.setAttachedFilePath(filePath.toString());
        }

        return ResponseEntity.ok(taskRepository.save(task));
    }

    @GetMapping
    public List<Task> getTasks(@RequestParam String username) {
        return taskRepository.findByUsername(username);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(title);
        task.setDescription(description);

        return getTaskResponseEntity(file, task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
