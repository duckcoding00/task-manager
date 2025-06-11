package task.manager.backend.repositories;

import java.time.OffsetDateTime;
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
import task.manager.backend.entity.TaskEntity;

@ApplicationScoped
public class TaskRepository {
    private static final Logger log = Logger.getLogger(TaskRepository.class);

    @Inject
    Pool client;

    // sql query
    private static final String INSERT_TASK = """
            insert into tasks(user_id, title, description, status, created_at, updated_at, duedated_at)
                values ($1, $2, $3, $4, $5, $6, $7)
                returning id
            """;

    private static final String GET_TASK_BY_ID = """
            select id, user_id, title, description, status, created_at, updated_at, duedated_at
            from tasks
            where id = $1
            """;

    private static final String GET_TASKS_BY_USER_ID = """
            select id, user_id, title, description, status, created_at, updated_at, duedated_at
            from tasks
            where user_id = $1
            """;

    private static final String DELETE_TASK = """
            delete from tasks where id = $1
            """;

    private static final String UPDATE_STATUS_TASK = """
            update tasks set status = $1, updated_at = $2 where id = $3
            """;

    // mapping
    private TaskEntity mappingEntity(Row row) {
        TaskEntity task = new TaskEntity();
        task.setId(row.getInteger("id"));
        task.setUserId(row.getInteger("user_id"));
        task.setTitle(row.getString("title"));
        task.setStatus(row.getString("status"));
        task.setDescription(row.getString("description"));
        task.setCreatedAt(row.getOffsetDateTime("created_at"));
        task.setUpdatedAt(row.getOffsetDateTime("updated_at"));
        task.setDuedatedAt(row.getOffsetDateTime("duedated_at"));

        return task;
    }

    public Uni<Integer> insert(TaskEntity task, SqlConnection conn) {
        OffsetDateTime now = OffsetDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        return conn.preparedQuery(INSERT_TASK).execute(
                Tuple.tuple()
                        .addValue(task.getUserId())
                        .addValue(task.getTitle())
                        .addValue(task.getDescription())
                        .addValue(task.getStatus())
                        .addValue(task.getCreatedAt())
                        .addValue(task.getUpdatedAt())
                        .addValue(task.getDuedatedAt()))
                .onItem().transform(rows -> {
                    return rows.iterator().next().getInteger("id");
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed insert task [%s]: %s", task.getTitle(), throwable.getMessage());
                });
    }

    public Uni<Integer> insert(TaskEntity task) {
        return client.withConnection(conn -> insert(task, conn));
    }

    public Uni<TaskEntity> get(Integer id, SqlConnection conn) {
        return conn.preparedQuery(GET_TASK_BY_ID).execute(Tuple.of(id))
                .onItem().transform(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? mappingEntity(iterator.next()) : null;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed get task [ID: %s]: %s", id, throwable.getMessage());
                });
    }

    public Uni<TaskEntity> get(Integer id) {
        return client.withConnection(conn -> get(id, conn));
    }

    public Uni<List<TaskEntity>> getAllByUser(Integer userId, SqlConnection conn) {
        return conn.preparedQuery(GET_TASKS_BY_USER_ID).execute(Tuple.of(userId))
                .onItem().transform(rows -> {
                    List<TaskEntity> tasks = new ArrayList<>();
                    rows.forEach(row -> tasks.add(mappingEntity(row)));
                    return tasks;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("Failed to get tasks for user [ID: %d]: %s", userId, throwable.getMessage());
                });
    }

    public Uni<List<TaskEntity>> getAllByUser(Integer userId) {
        return client.withConnection(conn -> getAllByUser(userId, conn));
    }

    public Uni<Void> deletedByID(Integer id, SqlConnection conn) {
        return conn.preparedQuery(DELETE_TASK)
                .execute(Tuple.of(id))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to delete task [ID: %id] : %s", id, throwable.getMessage());
                });
    }

    public Uni<Void> deletedByID(Integer id) {
        return client.withConnection(conn -> deletedByID(id, conn));
    }

    public Uni<Void> updateStatus(Integer id, String status, SqlConnection conn) {
        OffsetDateTime now = OffsetDateTime.now();

        return conn.preparedQuery(UPDATE_STATUS_TASK)
                .execute(Tuple.of(status, now, id))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to update status task [ID: %id] : %s", id, throwable.getMessage());
                });
    }

    public Uni<Void> updateStatus(Integer id, String status) {
        return client.withConnection(conn -> updateStatus(id, status, conn));
    }
}
