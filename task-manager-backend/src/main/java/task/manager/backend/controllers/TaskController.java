package task.manager.backend.controllers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import task.manager.backend.dto.request.TaskRequest;
import task.manager.backend.services.TaskService;
import task.manager.backend.utils.ApiResponse;

@Path("/tasks")
public class TaskController {

    @Inject
    TaskService taskService;

    @Inject
    JsonWebToken jwt;

    @Context
    HttpHeaders headers;

    @POST
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> insert(TaskRequest.insert request) {
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.create(userId, request))
                .map(taskId -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.CREATED.getStatusCode(),
                            "success insert task",
                            taskId);
                    return RestResponse.status(Response.Status.CREATED, response);
                });
    }

    @GET
    @Path("/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> get(@PathParam("id") String id) {
        Integer taskId = Integer.valueOf(id);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());

        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.task(taskId, userId))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get task",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getAll() {
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.tasks(userId))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get tasks",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> delete(@PathParam("id") String id) {
        Integer taskId = Integer.valueOf(id);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());

        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.delete(taskId, userId))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success delete task",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @PATCH
    @Path("/{id}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateStatus(@PathParam("id") String id,
            @QueryParam("status") String status) {
        Integer taskId = Integer.valueOf(id);

        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());

        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.updateStatus(taskId, userId, status))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success update status task",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @PATCH
    @Path("/{id}/update")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateTask(@PathParam("id") String id, TaskRequest.update request) {
        Integer taskId = Integer.valueOf(id);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> taskService.updateTask(taskId, userId, request))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success update task",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }
}
