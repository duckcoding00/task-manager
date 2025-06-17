package task.manager.backend.controllers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import task.manager.backend.dto.request.ProjectRequest;
import task.manager.backend.dto.request.TeamTaskRequest;
import task.manager.backend.services.ProjectService;
import task.manager.backend.services.TeamTaskService;
import task.manager.backend.utils.ApiResponse;

@Path("/projects/{projectId}")
public class ProjectTasksController {
    @Inject
    ProjectService projectService;

    @Inject
    TeamTaskService teamTaskService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getProject(@PathParam("projectId") String projectId) {
        Integer projectID = Integer.valueOf(projectId);
        return projectService.project(projectID)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> createProject(@PathParam("projectId") String projectId,
            TeamTaskRequest.insert request) {
        Integer projectID = Integer.valueOf(projectId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());

        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> teamTaskService.createTask(userId, projectID, request))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.CREATED.getStatusCode(),
                            "success create task",
                            result);
                    return RestResponse.status(Response.Status.CREATED, response);
                });

    }

    @PATCH
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateProject(@PathParam("projectId") String projectId,
            ProjectRequest.update request) {
        Integer projectID = Integer.valueOf(projectId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> projectService.updateProject(userId, projectID, request))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success update project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @DELETE
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> deleteProject(@PathParam("projectId") String projectId) {
        Integer projectID = Integer.valueOf(projectId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> projectService.deleteProject(userId, projectID))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success delete project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @PATCH
    @Path("/archive")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> archiveProject(
            @PathParam("projectId") String projectId,
            @QueryParam("status") @DefaultValue("archive") String status) {
        Integer projectID = Integer.valueOf(projectId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> projectService.archive(userId, projectID, status))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success archive project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }
}
