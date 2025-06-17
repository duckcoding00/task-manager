package task.manager.backend.services;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import task.manager.backend.dto.request.TeamTaskRequest;
import task.manager.backend.dto.response.TeamTaskResponse;
import task.manager.backend.entity.TeamTaskEntity;
import task.manager.backend.repositories.ProjectRepository;
import task.manager.backend.repositories.TeamTaskRepository;
import task.manager.backend.repositories.UserRepository;
import task.manager.backend.utils.exception.CustomException.InsertException;

@ApplicationScoped
public class TeamTaskService {

    private static final Logger log = Logger.getLogger(TeamTaskService.class);

    @Inject
    ProjectRepository projectRepository;

    @Inject
    TeamTaskRepository teamTaskRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    Pool client;

    public Uni<Integer> createTask(Integer userId, Integer projectId, @Valid TeamTaskRequest.insert request) {
        return client.withTransaction(conn -> {
            return userRepository.getById(userId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(String.format("user not found with [ID:%s]", userId)))
                    .flatMap(user -> {
                        return projectRepository.getById(projectId, conn)
                                .onItem().ifNull()
                                .failWith(() -> new NotFoundException(
                                        String.format("project not found with [ID:%s]", projectId)))
                                .flatMap(project -> {
                                    if (request.dueDate().isBefore(project.getStartDate())
                                            || request.dueDate().isAfter(project.getEndDate())) {
                                        throw new ValidationException(String.format("due date must between %s to %s",
                                                project.getStartDate(), project.getEndDate()));
                                    }

                                    TeamTaskEntity task = new TeamTaskEntity();
                                    task.setProjectId(projectId);
                                    task.setTitle(request.title());
                                    task.setDescription(request.description());
                                    task.setStatus("todo");
                                    task.setPriority(request.priority());
                                    task.setDueDate(request.dueDate());
                                    task.setCreatedBy(user.getUsername());

                                    return teamTaskRepository.insert(task, conn);
                                });
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("resource not found: %s", ex.getMessage());
                })
                .onFailure(ValidationException.class).invoke(ex -> {
                    log.errorf("validation error: %s", ex.getMessage());
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof ValidationException))
                .transform(throwable -> {
                    return new InsertException("Failed to create task: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<TeamTaskResponse.task> getTask(Integer taskId) {
        return teamTaskRepository.getTask(taskId)
                .onItem().ifNull()
                .failWith(() -> new NotFoundException("Task not found with ID: " + taskId))
                .map(data -> {
                    return new TeamTaskResponse.task(
                            data.getId(), // Fix: gunakan data.getId() bukan taskId parameter
                            data.getTitle(),
                            data.getDescription(),
                            data.getStatus(),
                            data.getPriority(),
                            data.getDueDate(),
                            data.getCreatedBy(),
                            data.getCreatedAt(),
                            data.getUpdatedAt());
                })
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to get task: " + throwable.getMessage(), throwable);
                });
    }
}
