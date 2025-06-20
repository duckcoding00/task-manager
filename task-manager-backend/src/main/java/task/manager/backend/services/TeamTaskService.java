package task.manager.backend.services;

import org.jboss.logging.Logger;

import io.quarkus.security.UnauthorizedException;
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
import task.manager.backend.repositories.ProjectMemberRepository;
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
    ProjectMemberRepository projectMemberRepository;

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

    public Uni<TeamTaskResponse.task> getTask(Integer taskId, Integer projectId) {
        return teamTaskRepository.getTask(taskId, projectId)
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

    public Uni<Void> deleteTask(Integer taskId, Integer projectId, Integer userID) {
        return client.withTransaction(conn -> {
            return teamTaskRepository.getTask(taskId, projectId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(
                            String.format("task not found with [ID:%s] and [ProjecTD:%s]", taskId, projectId)))
                    .flatMap(task -> {
                        return projectMemberRepository.getByUserIDAndProjectID(projectId, userID, conn)
                                .onItem().ifNull()
                                .failWith(() -> new NotFoundException(String
                                        .format("member not found with [ID:%s] at [ProjectID:%s]", userID, projectId)))
                                .flatMap(member -> {
                                    if (member.level() < 2) {
                                        throw new UnauthorizedException("access denied, must higher level access this");
                                    }

                                    return teamTaskRepository.deleteTask(taskId, projectId, conn);
                                });
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("resource not found : %s", ex.getMessage());
                })
                .onFailure(UnauthorizedException.class).invoke(ex -> {
                    log.errorf("%s", ex.getMessage());
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof UnauthorizedException))
                .transform(throwable -> {
                    return new InsertException("Failed to delete project: " + throwable.getMessage(), throwable);
                });

    }

    public Uni<Void> updateStatus(Integer taskId, Integer projectId, Integer userID, String status) {
        return client.withTransaction(conn -> {
            return teamTaskRepository.getTask(taskId, projectId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(
                            String.format("task not found with [ID:%s] and [ProjecTD:%s]", taskId, projectId)))
                    .flatMap(task -> {
                        return projectMemberRepository.getByUserIDAndProjectID(projectId, userID, conn)
                                .onItem().ifNull()
                                .failWith(() -> new NotFoundException(String
                                        .format("member not found with [ID:%s] at [ProjectID:%s]", userID, projectId)))
                                .flatMap(member -> {
                                    if (member.level() < 2) {
                                        throw new UnauthorizedException("access denied, must higher level access this");
                                    }

                                    return teamTaskRepository.updateStatus(taskId, projectId, status, conn);
                                });
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("resource not found : %s", ex.getMessage());
                })
                .onFailure(UnauthorizedException.class).invoke(ex -> {
                    log.errorf("%s", ex.getMessage());
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof UnauthorizedException))
                .transform(throwable -> {
                    return new InsertException("Failed to update project task status: " + throwable.getMessage(),
                            throwable);
                });

    }

    public Uni<Void> updatePriority(Integer taskId, Integer projectId, Integer userID, String priority) {
        return client.withTransaction(conn -> {
            return teamTaskRepository.getTask(taskId, projectId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(
                            String.format("task not found with [ID:%s] and [ProjecTD:%s]", taskId, projectId)))
                    .flatMap(task -> {
                        return projectMemberRepository.getByUserIDAndProjectID(projectId, userID, conn)
                                .onItem().ifNull()
                                .failWith(() -> new NotFoundException(String
                                        .format("member not found with [ID:%s] at [ProjectID:%s]", userID, projectId)))
                                .flatMap(member -> {
                                    if (member.level() < 2) {
                                        throw new UnauthorizedException("access denied, must higher level access this");
                                    }

                                    return teamTaskRepository.updatePriority(taskId, projectId, priority, conn);
                                });
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("resource not found : %s", ex.getMessage());
                })
                .onFailure(UnauthorizedException.class).invoke(ex -> {
                    log.errorf("%s", ex.getMessage());
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof UnauthorizedException))
                .transform(throwable -> {
                    return new InsertException("Failed to update project task priority: " + throwable.getMessage(),
                            throwable);
                });

    }

    private void updateField(TeamTaskEntity task, TeamTaskRequest.update request) {
        if (request.title() != null) {
            String trimTitle = request.title().trim();
            if (!trimTitle.isEmpty()) {
                task.setTitle(trimTitle);
            }
        }

        if (request.description() != null) {
            task.setDescription(request.description().trim());
        }
        if (request.dueDate() != null) {
            task.setDueDate(request.dueDate());
        }
    }

    public Uni<Integer> updateTask(Integer taskId, Integer projectId, Integer userId, TeamTaskRequest.update request) {
        return client.withTransaction(conn -> {
            return teamTaskRepository.getTask(taskId, projectId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(
                            String.format("task not found with [ID:%s] and [ProjecTD:%s]", taskId, projectId)))
                    .flatMap(task -> {
                        return projectMemberRepository.getByUserIDAndProjectID(projectId, userId, conn)
                                .onItem().ifNull()
                                .failWith(() -> new NotFoundException(String
                                        .format("member not found with [ID:%s] at [ProjectID:%s]", userId, projectId)))
                                .flatMap(member -> {
                                    if (member.level() < 2) {
                                        throw new UnauthorizedException("access denied, must higher level access this");
                                    }

                                    updateField(task, request);

                                    return teamTaskRepository.updateTask(task, conn);
                                });
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("resource not found : %s", ex.getMessage());
                })
                .onFailure(UnauthorizedException.class).invoke(ex -> {
                    log.errorf("%s", ex.getMessage());
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof UnauthorizedException))
                .transform(throwable -> {
                    return new InsertException("Failed to delete project task: " + throwable.getMessage(), throwable);
                });
    }

}
