package task.manager.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequest {
    public record register(
            @NotBlank(message = "username is required") @Size(min = 8, max = 50, message = "username must be at least {min} characters") String username,

            @NotBlank(message = "password is required") @Size(min = 8, max = 50, message = "password must be at least {min} characters") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "password must contain at least one digit, lowercase, uppercase, special character") String password) {
    }

    public record login(
            @NotBlank(message = "username is required") @Size(min = 8, max = 50, message = "username must be at least {min} characters") String username,

            @NotBlank(message = "password is required") @Size(min = 8, max = 50, message = "password must be at least {min} characters") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "password must contain at least one digit, lowercase, uppercase, special character") String password) {
    }
}
