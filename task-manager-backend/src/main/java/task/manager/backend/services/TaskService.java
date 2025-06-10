package task.manager.backend.services;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import task.manager.backend.dto.request.TaskRequest;
import task.manager.backend.dto.response.TaskResponse;
import task.manager.backend.entity.TaskEntity;
import task.manager.backend.repositories.TaskRepository;

@ApplicationScoped
public class TaskService {
    private static final Logger log = Logger.getLogger(TaskService.class);

    @Inject
    TaskRepository taskRepository;

    public Uni<Integer> create(Integer userId, @Valid TaskRequest.insert request) {
        return Uni.createFrom().item(() -> {
            TaskEntity task = new TaskEntity();
            task.setUserId(userId);
            task.setTitle(request.title());
            task.setStatus("todo");
            task.setDescription(request.description());
            task.setDuedatedAt(request.dueDate());
            return task;
        })
                .flatMap(taskRepository::insert)
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to create task: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<TaskResponse.task> task(Integer id, Integer userID) {
        return taskRepository.get(id)
                .onItem().transform(data -> {
                    if (data == null) {
                        throw new NotFoundException(String.format("task not found with [ID:%s]", id));
                    }

                    if (!data.getUserId().equals(userID)) {
                        throw new UnauthorizedException("access denied");
                    }

                    return new TaskResponse.task(
                            data.getId(),
                            data.getUserId(),
                            data.getTitle(),
                            data.getDescription(),
                            data.getStatus(),
                            data.getCreatedAt(),
                            data.getUpdatedAt(),
                            data.getDuedatedAt());
                })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("Task not found with [ID: %s]", id);
                })
                .onFailure(UnauthorizedException.class).invoke(ex -> {
                    log.error("unauthorized", ex);
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException)
                        && !(throwable instanceof UnauthorizedException))
                .transform(throwable -> {
                    return new RuntimeException("Failed to get task: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<List<TaskResponse.tasks>> tasks(Integer userId) {
        return taskRepository.getAllByUser(userId)
                .onItem().transform(data -> {
                    List<TaskResponse.tasks> tasks = new ArrayList<>();

                    data.forEach(row -> {
                        tasks.add(new TaskResponse.tasks(row.getId(), row.getTitle(), row.getDescription(),
                                row.getStatus(),
                                row.getDuedatedAt()));
                    });

                    return tasks;
                })
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to get tasks : " + throwable.getMessage(), throwable);
                });
    }
}
