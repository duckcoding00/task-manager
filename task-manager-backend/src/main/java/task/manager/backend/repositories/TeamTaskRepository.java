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
            select (id, title, description, status, priority, due_date)
            from team_tasks
            where project_id = $1
            """;

    private TeamTaskEntity mapping(Row row) {
        TeamTaskEntity tasks = new TeamTaskEntity();
        tasks.setId(row.getInteger("id"));
        tasks.setTitle(row.getString("title"));
        tasks.setDescription(row.getString("description"));
        tasks.setStatus(row.getString("status"));
        tasks.setPriority(row.getString("priority"));
        tasks.setDueDate(row.getLocalDate("due_date"));

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
}
