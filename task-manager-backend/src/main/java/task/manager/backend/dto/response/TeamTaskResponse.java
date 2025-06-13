package task.manager.backend.dto.response;

import java.time.LocalDate;

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
}
