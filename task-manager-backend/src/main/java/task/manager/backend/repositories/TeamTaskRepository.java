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
import task.manager.backend.entity.TeamTaskEntity;

@ApplicationScoped
public class TeamTaskRepository {
    private static final Logger log = Logger.getLogger(TeamTaskRepository.class);

    @Inject
    Pool client;

    private static final String INSERT_TEAM_TASK = """
            insert into team_tasks(project_id, title, description, status, priority, due_date, created_by, created_at, updated_at)
            values ($1, $2, $3, $4, $5, $6, $7, $8, $9)
            returning id
            """;

    private static final String GET_TASKS = """
            select id, project_id, title, description, status, priority, due_date, created_by, created_at, updated_at
            from team_tasks
            where project_id = $1
            """;

    private static final String GET_TASK_BY_ID = """
            select id, project_id, title, description, status, priority, due_date, created_by, created_at, updated_at
            from team_tasks
            where id = $1 and project_id = $2
            """;

    private static final String DELETE_TASK = """
            delete from team_tasks where id = $1 and project_id = $2
            """;

    private static final String UPDATE_STATUS_TASK = """
            update team_tasks set status = $1, updated_at = $2 where id = $3 and project_id = $4
            """;

    private static final String UPDATE_PRIORITY_TASK = """
            update team_tasks set priority = $1, updated_at = $2 where id = $3 and project_id = $4
            """;

    private static final String UPDATE_TASK = """
            update team_tasks set title = $1, description = $2, due_date = $3, updated_at = $4 where id = $5 and project_id = $6
            returning id
            """;

    private TeamTaskEntity mapping(Row row) {
        TeamTaskEntity tasks = new TeamTaskEntity();
        tasks.setId(row.getInteger("id"));
        tasks.setProjectId(row.getInteger("project_id"));
        tasks.setTitle(row.getString("title"));
        tasks.setDescription(row.getString("description"));
        tasks.setStatus(row.getString("status"));
        tasks.setPriority(row.getString("priority"));
        tasks.setDueDate(row.getLocalDate("due_date"));
        tasks.setCreatedBy(row.getString("created_by"));
        tasks.setCreatedAt(row.getLocalDateTime("created_at"));
        tasks.setUpdatedAt(row.getLocalDateTime("updated_at"));

        return tasks;
    }

    public Uni<Integer> insert(TeamTaskEntity data, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        data.setCreatedAt(now);
        data.setUpdatedAt(now);

        return conn.preparedQuery(INSERT_TEAM_TASK)
                .execute(Tuple.tuple()
                        .addValue(data.getProjectId())
                        .addValue(data.getTitle())
                        .addValue(data.getDescription())
                        .addValue(data.getStatus())
                        .addValue(data.getPriority())
                        .addValue(data.getDueDate())
                        .addValue(data.getCreatedBy())
                        .addValue(data.getCreatedAt())
                        .addValue(data.getUpdatedAt()))
                .onItem().transform(row -> {
                    return row.iterator().next().getInteger("id");
                }).onFailure().invoke(throwable -> {
                    log.errorf("failed insert task [%s]: %s", data.getTitle(), throwable.getMessage());
                });
    }

    public Uni<Integer> insert(TeamTaskEntity data) {
        return client.withConnection(conn -> insert(data, conn));
    }

    public Uni<List<TeamTaskEntity>> getTasks(Integer projectId, SqlConnection conn) {
        return conn.preparedQuery(GET_TASKS)
                .execute(Tuple.of(projectId))
                .onItem().transform(rows -> {
                    List<TeamTaskEntity> tasks = new ArrayList<>();
                    rows.forEach(row -> tasks.add(mapping(row)));
                    return tasks;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get tasks [ProjectID: %d]: %s", projectId, throwable.getMessage());
                });
    }

    public Uni<List<TeamTaskEntity>> getTasks(Integer projectId) {
        return client.withConnection(conn -> getTasks(projectId, conn));
    }

    public Uni<TeamTaskEntity> getTask(Integer taskId, Integer projectId, SqlConnection conn) {
        return conn.preparedQuery(GET_TASK_BY_ID)
                .execute(Tuple.of(taskId, projectId))
                .onItem().transform(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? mapping(iterator.next()) : null;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get task [TaskID: %d]: %s", taskId, throwable.getMessage());
                });
    }

    public Uni<TeamTaskEntity> getTask(Integer taskId, Integer projectId) {
        return client.withConnection(conn -> getTask(taskId, projectId, conn));
    }

    public Uni<Void> deleteTask(Integer taskId, Integer projectId, SqlConnection conn) {
        return conn.preparedQuery(DELETE_TASK)
                .execute(Tuple.of(taskId, projectId))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to delete task [ID:%s] and [ProjectID:%s]: %s", taskId, projectId,
                            throwable.getMessage());
                });
    }

    public Uni<Void> deleteTask(Integer taskId, Integer projectId) {
        return client.withConnection(conn -> deleteTask(taskId, projectId, conn));
    }

    public Uni<Void> updateStatus(Integer taskId, Integer projectId, String status, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        return conn.preparedQuery(UPDATE_STATUS_TASK)
                .execute(Tuple.of(status, now, taskId, projectId))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to update status task [ID:%s] and [ProjectID:%s]: %s", taskId, projectId,
                            throwable.getMessage());
                });
    }

    public Uni<Void> updateStatus(Integer taskId, Integer projectId, String status) {
        return client.withConnection(conn -> updateStatus(taskId, projectId, status, conn));
    }

    public Uni<Void> updatePriority(Integer taskId, Integer projectId, String priority, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        return conn.preparedQuery(UPDATE_PRIORITY_TASK)
                .execute(Tuple.of(priority, now, taskId, projectId))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to update priority task [ID:%s] and [ProjectID:%s]: %s", taskId, projectId,
                            throwable.getMessage());
                });
    }

    public Uni<Void> updatePriority(Integer taskId, Integer projectId, String priority) {
        return client.withConnection(conn -> updatePriority(taskId, projectId, priority, conn));
    }

    public Uni<Integer> updateTask(TeamTaskEntity task, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        return conn.preparedQuery(UPDATE_TASK)
                .execute(Tuple.of(
                        task.getTitle(), task.getDescription(), task.getDueDate(), now, task.getId(),
                        task.getProjectId()))
                .onItem().transform(rows -> {
                    return rows.iterator().next().getInteger("id");
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to update task [ID:%s] and [ProjectID:%s]: %s", task.getId(),
                            task.getProjectId(), throwable.getMessage());
                });
    }

    public Uni<Integer> updateTask(TeamTaskEntity task) {
        return client.withConnection(conn -> updateTask(task, conn));
    }
}
