package task.manager.backend.services;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import task.manager.backend.dto.request.TeamTaskRequest;
import task.manager.backend.entity.TeamTaskEntity;
import task.manager.backend.repositories.TeamTaskRepository;
import task.manager.backend.repositories.UserRepository;
import task.manager.backend.utils.exception.CustomException.InsertException;

@ApplicationScoped
public class TeamTaskService {
    @Inject
    TeamTaskRepository teamTaskRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    Pool client;

    public Uni<Integer> createTask(Integer userId, Integer projectId, @Valid TeamTaskRequest.insert request) {
        return client.withTransaction(conn -> {
            return userRepository.getById(userId, conn)
                    .flatMap(user -> {
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
        })
                .onFailure().transform(throwable -> {
                    return new InsertException("failed to create task: " + throwable.getMessage(), throwable);
                });
    }
}
