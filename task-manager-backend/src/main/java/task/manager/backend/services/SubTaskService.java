package task.manager.backend.services;

import java.util.List;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import task.manager.backend.dto.request.TeamTaskRequest;
import task.manager.backend.entity.SubTaskEntity;
import task.manager.backend.repositories.SubTaskRepository;
import task.manager.backend.repositories.SubTaskRepository.TaskType;

@ApplicationScoped
public class SubTaskService {
    private static final Logger log = Logger.getLogger(SubTaskService.class);

    @Inject
    SubTaskRepository subTaskRepository;

    public Uni<Integer> insert(Integer taskId, @Valid TeamTaskRequest.subtask task, TaskType taskType) {
        SubTaskEntity newTask = new SubTaskEntity();
        newTask.setTask(task.task());
        newTask.setTaskId(taskId);

        return subTaskRepository.insert(newTask, taskType)
                .onFailure().transform(throwable -> {
                    log.errorf("Failed to insert subtask for taskId %d with type %s: %s",
                            taskId, taskType, throwable.getMessage());
                    return new RuntimeException("Insert failed", throwable);
                });
    }

    public Uni<Void> updateStatus(Integer id, TaskType taskType) {
        return subTaskRepository.updateStatus(id, taskType)
                .onFailure().transform(throwable -> {
                    log.errorf("Failed to update status subtask id %d with type %s: %s",
                            id, taskType, throwable.getMessage());
                    return new RuntimeException("Update status failed", throwable);
                });
    }

    public Uni<Void> deleteSubtask(Integer id, TaskType taskType) {
        return subTaskRepository.delete(id, taskType)
                .onFailure().transform(throwable -> {
                    log.errorf("Failed to delete subtask id %d with type %s: %s",
                            id, taskType, throwable.getMessage());
                    return new RuntimeException("Delete failed", throwable);
                });
    }

    public Uni<List<SubTaskEntity>> getAll(Integer taskId, TaskType taskType) {
        return subTaskRepository.getAll(taskId, taskType)
                .onFailure().transform(throwable -> {
                    log.errorf("Failed to get all subtasks for taskId %d with type %s: %s",
                            taskId, taskType, throwable.getMessage());
                    return new RuntimeException("Get all subtasks failed", throwable);
                });
    }

    public Uni<SubTaskEntity> getById(Integer id, TaskType taskType) {
        return subTaskRepository.getById(id, taskType)
                .onFailure().transform(throwable -> {
                    log.errorf("Failed to get subtask id %d with type %s: %s",
                            id, taskType, throwable.getMessage());
                    return new RuntimeException("Get subtask failed", throwable);
                });
    }
}
