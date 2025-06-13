package task.manager.backend.entity;

import java.time.LocalDateTime;

public class TaskAssignmentEntity {
    private Integer id;
    private Integer taskId;
    private Integer userId;
    private Integer assignedBy;
    private LocalDateTime assignedAt;

    // Default constructor
    public TaskAssignmentEntity() {
    }

    // Constructor with all fields
    public TaskAssignmentEntity(Integer id, Integer taskId, Integer userId, Integer assignedBy,
            LocalDateTime assignedAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Integer assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
