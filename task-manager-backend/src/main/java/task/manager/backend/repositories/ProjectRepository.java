package task.manager.backend.repositories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import task.manager.backend.dto.response.ProjectResponse;
import task.manager.backend.entity.ProjectEntity;

@ApplicationScoped
public class ProjectRepository {
    private static final Logger log = Logger.getLogger(ProjectRepository.class);

    @Inject
    Pool client;

    private static final String INSERT_PROJECT = """
            insert into projects (name, description, status, start_date, end_date, created_by, created_at, updated_at)
            values ($1, $2, $3, $4, $5, $6, $7, $8)
            returning id
            """;

    private static final String GET_PROJECT = """
            select id, name, description, status, start_date, end_date, created_by, created_at, updated_at, is_deleted, deleted_at
            from projects where id = $1
            """;

    private static final String GET_PROJECTS_BY_USER = """
            select p.id, p.name, p.description, p.status, p.start_date, p.end_date, m.user_id, m.role_id, m.joined_at, m.left_at, m.status
            from projects p
            inner join project_members m on p.id = m.project_id
            where m.user_id = $1
            """;

    private ProjectEntity mappingEntity(Row row) {
        ProjectEntity project = new ProjectEntity();
        project.setId(row.getInteger("id"));
        project.setName(row.getString("name"));
        project.setDescription(row.getString("description"));
        project.setStatusProject(row.getString("status"));
        project.setStartDate(row.getLocalDate("start_date"));
        project.setEndDate(row.getLocalDate("end_date"));
        project.setCreatedBy(row.getString("created_by"));
        project.setCreatedAt(row.getLocalDateTime("created_at"));
        project.setUpdatedAt(row.getLocalDateTime("updated_at"));
        project.setIsDeleted(row.getBoolean("is_deleted"));
        project.setDeletedAt(row.getLocalDateTime("deleted_at"));

        return project;
    }

    public Uni<Integer> insert(ProjectEntity project, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        return conn.preparedQuery(INSERT_PROJECT)
                .execute(Tuple.tuple()
                        .addValue(project.getName())
                        .addValue(project.getDescription())
                        .addValue(project.getStatusProject())
                        .addValue(project.getStartDate())
                        .addValue(project.getEndDate())
                        .addValue(project.getCreatedBy())
                        .addValue(project.getCreatedAt())
                        .addValue(project.getUpdatedAt()))
                .onItem().transform(rows -> {
                    return rows.iterator().next().getInteger("id");
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed insert project [%s]: %s", project.getName(), throwable);
                });
    }

    public Uni<Integer> insert(ProjectEntity project) {
        return client.withConnection(conn -> insert(project, conn));
    }

    public Uni<ProjectEntity> getById(Integer id, SqlConnection conn) {
        return conn.preparedQuery(GET_PROJECT)
                .execute(Tuple.of(id))
                .onItem().transform(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? mappingEntity(iterator.next()) : null;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed get project [ID: %d]: %s", id, throwable);
                });
    }

    public Uni<ProjectEntity> getById(Integer id) {
        return client.withConnection(conn -> getById(id, conn));
    }

    public Uni<List<ProjectResponse.Projects>> getProjects(Integer userId, SqlConnection conn) {
        return conn.preparedQuery(GET_PROJECTS_BY_USER)
                .execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    List<ProjectResponse.Projects> projects = new ArrayList<>();

                    rows.forEach(row -> {
                        ProjectResponse.Projects project = new ProjectResponse.Projects(
                                row.getInteger("id"),
                                row.getString("name"),
                                row.getString("description"),
                                row.getString("status"),
                                row.getLocalDate("start_date"),
                                row.getLocalDate("end_date"));

                        projects.add(project);
                    });

                    return projects;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed get projects by user [ID: %d]: %s", userId, throwable);
                });
    }

    public Uni<List<ProjectResponse.Projects>> getProjects(Integer userId) {
        return client.withConnection(conn -> getProjects(userId, conn));
    }
}
