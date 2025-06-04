package task.manager.backend.utils;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        @JsonProperty("status_code") Integer statusCode,

        String errors,

        String message,

        @JsonFormat(pattern = "yyyy-mm-dd HH:mm:ss") LocalDateTime timestamp,

        Map<String, Object> details,

        T data) {

    private static final LocalDateTime now = LocalDateTime.now().withNano(0);

    public static <T> ApiResponse<T> of(Integer statusCode, String error, String message, Map<String, Object> details,
            T data) {
        return new ApiResponse<T>(statusCode, error, message, now, details, data);
    }

    public static <T> ApiResponse<T> success(Integer statusCode, String message, T data) {
        return new ApiResponse<T>(statusCode, null, message, now, null, data);
    }

    public static <T> ApiResponse<T> error(Integer statusCode, String errors, Map<String, Object> details) {
        return new ApiResponse<T>(statusCode, errors, null, now, details, null);
    }
}
