package task.manager.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {
    public record login(
            String username,

            @JsonProperty("access_token") String accessToken) {
    }
}
