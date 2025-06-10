package task.manager.backend.entity;

import java.time.OffsetDateTime;

public class TaskEntity {
    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime duedatedAt;

    // Default constructor
    public TaskEntity() {
    }

    // All-args constructor
    public TaskEntity(Integer id, Integer userId, String title, String description,
            String status, OffsetDateTime createdAt, OffsetDateTime updatedAt,
            OffsetDateTime duedatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.duedatedAt = duedatedAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getDuedatedAt() {
        return duedatedAt;
    }

    public void setDuedatedAt(OffsetDateTime duedatedAt) {
        this.duedatedAt = duedatedAt;
    }
}
