package task.manager.backend.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectMemberResponse {
    public record member(
            Integer id,
            String username,
            @JsonProperty("project_id") Integer projectId,
            @JsonProperty("user_id") Integer userId,
            @JsonProperty("role_id") Integer roleId,
            @JsonProperty("joined_at") LocalDateTime joinedAt,
            @JsonProperty("left_at") LocalDateTime leftAt,
            String status,
            Integer level,
            String description

    ) {
    }
}
