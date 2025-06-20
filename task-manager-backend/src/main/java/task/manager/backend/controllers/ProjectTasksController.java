package task.manager.backend.controllers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import task.manager.backend.dto.request.ProjectRequest;
import task.manager.backend.dto.request.TeamTaskRequest;
import task.manager.backend.repositories.SubTaskRepository.TaskType;
import task.manager.backend.services.ProjectService;
import task.manager.backend.services.SubTaskService;
import task.manager.backend.services.TeamTaskService;
import task.manager.backend.utils.ApiResponse;

@Path("/projects/{projectId}")
public class ProjectTasksController {
    @Inject
    ProjectService projectService;

    @Inject
    TeamTaskService teamTaskService;

    @Inject
    SubTaskService subTaskService;

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

    @GET
    @Path("/task/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getTask(
            @PathParam("projectId") String projectId,
            @PathParam("taskId") String taskId) {
        Integer projectID = Integer.valueOf(projectId);
        Integer taskID = Integer.valueOf(taskId);
        return teamTaskService.getTask(taskID, projectID)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get project task",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @DELETE
    @Path("/task/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> deleteTask(
            @PathParam("projectId") String projectId,
            @PathParam("taskId") String taskId) {
        Integer projectID = Integer.valueOf(projectId);
        Integer taskID = Integer.valueOf(taskId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> teamTaskService.deleteTask(taskID, projectID, userId))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success delete project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });

    }

    @PATCH
    @Path("/task/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateStatusTask(
            @PathParam("projectId") String projectId,
            @PathParam("taskId") String taskId,
            @QueryParam("status") String status,
            @QueryParam("priority") String priority) {
        Integer projectID = Integer.valueOf(projectId);
        Integer taskID = Integer.valueOf(taskId);

        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> {
                    if (status != null && !status.isEmpty()) {
                        return teamTaskService.updateStatus(taskID, projectID, userId, status);
                    } else if (priority != null && !priority.isEmpty()) {
                        return teamTaskService.updatePriority(taskID, projectID, userId, priority);
                    } else {
                        throw new ValidationException("Either status or priority must be provided");
                    }
                })
                .map(result -> {
                    String message = (status != null && !status.isEmpty()) ? "success update task status"
                            : "success update task priority";
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            message,
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @PUT
    @Path("/task/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateTask(
            @PathParam("projectId") String projectId,
            @PathParam("taskId") String taskId,
            TeamTaskRequest.update request) {
        Integer projectID = Integer.valueOf(projectId);
        Integer taskID = Integer.valueOf(taskId);
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(userId -> teamTaskService.updateTask(taskID, projectID, userId, request))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success Update project",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });

    }

    @POST
    @Path("/task/{id}/subtask")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> createSubTask(
            @PathParam("id") String id,
            TeamTaskRequest.subtask task) {
        Integer taskId = Integer.valueOf(id);
        return subTaskService.insert(taskId, task, TaskType.TEAM_SUB_TASKS)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.CREATED.getStatusCode(),
                            "success create subtask",
                            result);
                    return RestResponse.status(Response.Status.CREATED, response);
                });
    }

    @GET
    @Path("/task/{id}/subtask")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getAll(
            @PathParam("id") String id) {
        Integer taskId = Integer.valueOf(id);
        return subTaskService.getAll(taskId, TaskType.TEAM_SUB_TASKS)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.CREATED.getStatusCode(),
                            "success create subtask",
                            result);
                    return RestResponse.status(Response.Status.CREATED, response);
                });
    }

    @PATCH
    @Path("/task/{id}/subtask/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> updateSttausSubTask(
            @PathParam("id") String id,
            @PathParam("taskId") String taskID,
            String task) {
        Integer taskId = Integer.valueOf(id);
        Integer subTaskId = Integer.valueOf(taskID);
        return subTaskService.updateStatus(subTaskId, TaskType.TEAM_SUB_TASKS)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success update status subtask",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    @DELETE
    @Path("/task/{id}/subtask/{taskId}")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> deleteSubTask(
            @PathParam("id") String id,
            @PathParam("taskId") String taskID,
            String task) {
        Integer taskId = Integer.valueOf(id);
        Integer subTaskId = Integer.valueOf(taskID);
        return subTaskService.deleteSubtask(subTaskId, TaskType.TEAM_SUB_TASKS)
                .onItem().transform(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success delete subtask",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }
}
