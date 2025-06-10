package task.manager.backend.controllers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
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
        Integer userId = Integer.valueOf(jwt.getClaim("userId").toString());

        return taskService.create(userId, request)
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
        Integer userId = Integer.valueOf(jwt.getClaim("userId").toString());
        Integer taskId = Integer.valueOf(id);

        return taskService.task(taskId, userId)
                .map(task -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get task",
                            task);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getAll() {
        Integer userId = Integer.valueOf(jwt.getClaim("userId").toString());

        return taskService.tasks(userId)
                .map(tasks -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get tasks",
                            tasks);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }
}
