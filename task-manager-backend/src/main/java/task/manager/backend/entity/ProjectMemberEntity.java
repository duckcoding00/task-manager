package task.manager.backend.entity;

import java.time.LocalDateTime;

public class ProjectMemberEntity {
    private Integer id;
    private Integer projectId;
    private Integer userId;
    private Integer roleId;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String memberStatus;
    private LocalDateTime deletedAt;

    // Default constructor
    public ProjectMemberEntity() {
    }

    // Constructor with all fields
    public ProjectMemberEntity(Integer id, Integer projectId, Integer userId, Integer roleId,
            LocalDateTime joinedAt, LocalDateTime leftAt, String memberStatus,
            LocalDateTime deletedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.roleId = roleId;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.memberStatus = memberStatus;
        this.deletedAt = deletedAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
