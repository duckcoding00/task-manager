package task.manager.backend.dto.request;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {
    public record insert(
            @JsonProperty("user_id") Integer userId,

            @NotBlank(message = "title is required") String title,

            @NotBlank(message = "description is required") String description,

            @JsonProperty("due_date") @NotNull(message = "due_date is required") OffsetDateTime dueDate) {
    }

    public record update(
            String title,

            String description,

            @JsonProperty("due_date") OffsetDateTime dueDate) {
    }
}
