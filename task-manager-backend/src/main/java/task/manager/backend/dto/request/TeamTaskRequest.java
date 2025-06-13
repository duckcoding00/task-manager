package task.manager.backend.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeamTaskRequest {
    public record insert(
            @NotBlank(message = "title is required") String title,

            @NotBlank(message = "description is required") String description,

            @NotNull(message = "due_date is required") @JsonProperty("due_date") LocalDate dueDate,

            @NotBlank(message = "priority is required") String priority) {
    }
}
