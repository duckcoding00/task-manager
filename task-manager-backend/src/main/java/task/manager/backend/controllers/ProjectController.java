package task.manager.backend.controllers;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
//import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import task.manager.backend.dto.request.ProjectRequest;
import task.manager.backend.services.ProjectService;
import task.manager.backend.utils.ApiResponse;

@Path("/projects")
public class ProjectController {
    @Inject
    ProjectService projectService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> insert(ProjectRequest.insert request) {
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(id -> projectService.createProject(id, request))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.CREATED.getStatusCode(),
                            "success insert task",
                            result);
                    return RestResponse.status(Response.Status.CREATED, response);
                });
    }

    @GET
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> getProjects() {
        return Uni.createFrom().item(() -> {
            return Integer.valueOf(jwt.getClaim("userId").toString());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(id -> projectService.getProjects(id))
                .map(result -> {
                    ApiResponse<Object> response = ApiResponse.success(
                            Response.Status.OK.getStatusCode(),
                            "success get tasks",
                            result);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

    // @GET
    // @Authenticated
    // @Path("/{id}")
    // @Produces(MediaType.APPLICATION_JSON)
    // public Uni<RestResponse<ApiResponse<Object>>> getProject(@PathParam("id")
    // String id) {
    // Integer projectID = Integer.valueOf(id);
    // return projectService.project(projectID)
    // .onItem().transform(result -> {
    // ApiResponse<Object> response = ApiResponse.success(
    // Response.Status.CREATED.getStatusCode(),
    // "success insert task",
    // result);
    // return RestResponse.status(Response.Status.CREATED, response);
    // });
    // }

}
