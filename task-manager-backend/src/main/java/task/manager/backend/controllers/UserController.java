package task.manager.backend.controllers;

//import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import task.manager.backend.dto.request.UserRequest;
import task.manager.backend.services.UserService;
import task.manager.backend.utils.ApiResponse;

@Path("/user")
public class UserController {
    // private static final Logger log = Logger.getLogger(UserController.class);

    @Inject
    UserService userService;

    @Context
    HttpHeaders headers;

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> register(UserRequest.register request) {
        return userService.registerUser(request)
                .onItem().transform(data -> {
                    ApiResponse<Object> response = ApiResponse.success(Response.Status.CREATED.getStatusCode(),
                            "success register", data);
                    return RestResponse.status(Response.Status.CREATED, response);
                });
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<RestResponse<ApiResponse<Object>>> register(UserRequest.login request) {
        return userService.loginUser(request)
                .onItem().transform(data -> {
                    ApiResponse<Object> response = ApiResponse.success(Response.Status.CREATED.getStatusCode(),
                            "success register", data);
                    return RestResponse.status(Response.Status.OK, response);
                });
    }

}
