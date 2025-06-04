package task.manager.backend.utils.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import task.manager.backend.utils.ApiResponse;

@ApplicationScoped
public class GlobalException {
    @ServerExceptionMapper
    public Uni<RestResponse<ApiResponse<Object>>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> errors = Map.of(
                "error", e.getMessage());

        ApiResponse<Object> error = ApiResponse.error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "RUNTIME_ERROR", errors);

        return Uni.createFrom().item(RestResponse.status(Response.Status.BAD_REQUEST, error));
    }

    @ServerExceptionMapper
    public Uni<RestResponse<ApiResponse<Object>>> handleValidationException(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ApiResponse<Object> error = ApiResponse.error(Response.Status.BAD_REQUEST.getStatusCode(), "VALIDATION_ERROR",
                Map.of("validations", errors));

        return Uni.createFrom().item(RestResponse.status(Response.Status.BAD_REQUEST, error));
    }
}
