package task.manager.backend.utils.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.UnexpectedTypeException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import task.manager.backend.utils.ApiResponse;

@ApplicationScoped
public class GlobalException {
        @ServerExceptionMapper
        public Uni<RestResponse<ApiResponse<Object>>> handleRuntimeException(RuntimeException e) {
                Map<String, Object> errors = Map.of(
                                "error", e.getMessage() != null ? e.getMessage() : "Unknown error");

                ApiResponse<Object> error = ApiResponse.error(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                "RUNTIME_ERROR", errors);

                return Uni.createFrom().item(RestResponse.status(Response.Status.BAD_REQUEST, error));
        }

        @ServerExceptionMapper
        public Uni<RestResponse<ApiResponse<Object>>> handleValidationException(ConstraintViolationException e) {
                List<String> errors = e.getConstraintViolations()
                                .stream()
                                .map(violation -> violation.getMessage() != null ? violation.getMessage()
                                                : "Invalid value")
                                .collect(Collectors.toList());

                ApiResponse<Object> error = ApiResponse.error(Response.Status.BAD_REQUEST.getStatusCode(),
                                "VALIDATION_ERROR",
                                Map.of("validations", errors));

                return Uni.createFrom().item(RestResponse.status(Response.Status.BAD_REQUEST, error));
        }

        @ServerExceptionMapper
        public Uni<RestResponse<ApiResponse<Object>>> handleUnexpectedTypeException(UnexpectedTypeException e) {
                ApiResponse<Object> error = ApiResponse.error(
                                Response.Status.BAD_REQUEST.getStatusCode(),
                                "UNEXPECTED_ERROR",
                                Map.of("error", "Invalid validation constraint: "
                                                + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
                return Uni.createFrom().item(RestResponse.status(Response.Status.BAD_REQUEST, error));
        }

        @ServerExceptionMapper
        public Uni<RestResponse<ApiResponse<Object>>> handlenotfound(NotFoundException e) {
                Map<String, Object> errors = Map.of(
                                "error", e.getMessage() != null ? e.getMessage() : "Resource not found");

                ApiResponse<Object> error = ApiResponse.error(Response.Status.NOT_FOUND.getStatusCode(),
                                "NOT_FOUND", errors);

                return Uni.createFrom().item(RestResponse.status(Response.Status.NOT_FOUND, error));
        }

        @ServerExceptionMapper
        public Uni<RestResponse<ApiResponse<Object>>> handlenotfound(UnauthorizedException e) {
                Map<String, Object> errors = Map.of(
                                "error", e.getMessage() != null ? e.getMessage() : "Unauthorized access");

                ApiResponse<Object> error = ApiResponse.error(Response.Status.UNAUTHORIZED.getStatusCode(),
                                "UNAUTHORIZED", errors);

                return Uni.createFrom().item(RestResponse.status(Response.Status.UNAUTHORIZED, error));
        }
}
