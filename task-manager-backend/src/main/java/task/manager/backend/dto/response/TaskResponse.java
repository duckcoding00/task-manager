package task.manager.backend.dto.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskResponse {
        public record task(
                        Integer id,
                        @JsonProperty("user_id") Integer userId,
                        String title,
                        String description,
                        String status,

                        @JsonProperty("created_at") OffsetDateTime createdAt,
                        @JsonProperty("updated_at") OffsetDateTime updatedAt,
                        @JsonProperty("due_dated_at") OffsetDateTime dueDatedAt) {
        }

        public record tasks(
                        Integer id,
                        String title,
                        String description,
                        String status,

                        @JsonProperty("due_dated_at") OffsetDateTime dueDatedAt) {
        }
}
