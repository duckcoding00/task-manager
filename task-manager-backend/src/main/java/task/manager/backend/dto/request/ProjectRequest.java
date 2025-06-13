package task.manager.backend.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectRequest {
    public record insert(
            @NotBlank(message = "name is required") String name,

            @NotBlank(message = "description is required") String description,

            @JsonProperty("start_date") @NotNull(message = "start_date is required") LocalDate startDate,

            @JsonProperty("end_date") @NotNull(message = "end_date is required") LocalDate endDate

    ) {
    }
}
