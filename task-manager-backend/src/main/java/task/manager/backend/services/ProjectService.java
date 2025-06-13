package task.manager.backend.services;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import task.manager.backend.dto.request.ProjectRequest;
import task.manager.backend.dto.response.ProjectResponse;
import task.manager.backend.dto.response.TeamTaskResponse;
import task.manager.backend.entity.ProjectEntity;
import task.manager.backend.entity.ProjectMemberEntity;
import task.manager.backend.repositories.ProjectMemberRepository;
import task.manager.backend.repositories.ProjectRepository;
import task.manager.backend.repositories.TeamTaskRepository;
import task.manager.backend.repositories.UserRepository;
import task.manager.backend.utils.exception.CustomException.InsertException;

@ApplicationScoped
public class ProjectService {
    private static final Logger log = Logger.getLogger(ProjectService.class);

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectMemberRepository projectMemberRepository;

    @Inject
    TeamTaskRepository teamTaskRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    Pool client;

    public Uni<Integer> createProject(Integer userId, @Valid ProjectRequest.insert request) {

        return client.withTransaction(conn -> {
            return userRepository.getById(userId, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(String.format("user not found with [ID:%s]", userId)))
                    .flatMap(user -> {
                        ProjectEntity project = new ProjectEntity();
                        project.setName(request.name());
                        project.setDescription(request.description());
                        project.setStatusProject("active");
                        project.setCreatedBy(user.getUsername());
                        project.setStartDate(request.startDate());
                        project.setEndDate(request.endDate());

                        return projectRepository.insert(project, conn)
                                .flatMap(projectId -> {
                                    ProjectMemberEntity member = new ProjectMemberEntity();
                                    member.setProjectId(projectId);
                                    member.setUserId(userId);
                                    member.setRoleId(1);

                                    return projectMemberRepository.insert(member, conn)
                                            .map(memberId -> projectId);
                                });
                    });

        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("user not found with [ID: %s]", userId);
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException))
                .transform(throwable -> {
                    return new InsertException("Failed to create project: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<List<ProjectResponse.Projects>> getProjects(Integer userId) {
        return projectRepository.getProjects(userId)
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to get projects: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<ProjectResponse.ProjectTasks> project(Integer projectID) {
        return client.withTransaction(conn -> {
            return projectRepository.getById(projectID, conn)
                    .onItem().ifNull()
                    .failWith(() -> new NotFoundException(String.format("project not found with [ID:%s]", projectID)))
                    .flatMap(resultProject -> {
                        ProjectResponse.Project project = new ProjectResponse.Project(
                                resultProject.getId(),
                                resultProject.getName(),
                                resultProject.getDescription(),
                                resultProject.getStatusProject(),
                                resultProject.getCreatedBy(),
                                resultProject.getStartDate(),
                                resultProject.getEndDate(),
                                resultProject.getCreatedAt(),
                                resultProject.getUpdatedAt());

                        return teamTaskRepository.getTasks(projectID, conn)
                                .onItem().transform(resultTask -> {
                                    List<TeamTaskResponse.tasks> tasks = new ArrayList<>();
                                    resultTask.forEach(row -> {
                                        tasks.add(new TeamTaskResponse.tasks(
                                                row.getId(),
                                                row.getTitle(),
                                                row.getDescription(),
                                                row.getStatus(),
                                                row.getPriority(),
                                                row.getDueDate()));
                                    });
                                    return tasks;
                                })
                                .map(tasks -> new ProjectResponse.ProjectTasks(project, tasks));
                    });
        })
                .onFailure(NotFoundException.class).invoke(ex -> {
                    log.errorf("project not found with [ID: %s]", projectID);
                })
                .onFailure(throwable -> !(throwable instanceof NotFoundException))
                .transform(throwable -> {
                    return new RuntimeException("Failed to get project details: " + throwable.getMessage(), throwable);
                });
    }
}
