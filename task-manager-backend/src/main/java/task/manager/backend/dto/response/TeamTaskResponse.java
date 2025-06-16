package task.manager.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TeamTaskResponse {
    public record tasks(
            Integer id,
            String title,
            String description,
            String status,
            String priority,
            @JsonProperty("due_date") LocalDate dueDate) {
    }

    public record task(
            Integer id,
            String title,
            String description,
            String status,
            String priority,
            @JsonProperty("due_date") LocalDate dueDate,
            @JsonProperty("created_by") String createdBy,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("updated_at") LocalDateTime updatedAt) {
    }
}
