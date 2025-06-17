package task.manager.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectResponse {
        public record Projects(
                        Integer id,
                        String name,
                        String description,
                        String status,

                        @JsonProperty("start_date") LocalDate startDate,

                        @JsonProperty("end_date") LocalDate endDate) {
        }

        public record Project(
                        Integer id,
                        String name,
                        String description,
                        String status,
                        String createdBy,
                        @JsonProperty("start_date") LocalDate startDate,

                        @JsonProperty("end_date") LocalDate endDate,

                        @JsonProperty("created_at") LocalDateTime createdAt,
                        @JsonProperty("updated_at") LocalDateTime updatedAt) {
        }

        public record ProjectTasks(
                        Project project,
                        List<ProjectMemberResponse.member> members,
                        List<TeamTaskResponse.tasks> tasks) {
        }
}
