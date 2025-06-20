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
import task.manager.backend.entity.SubTaskEntity;

@ApplicationScoped
public class SubTaskRepository {
    private static final Logger log = Logger.getLogger(SubTaskRepository.class);

    @Inject
    Pool client;

    public enum TaskType {
        SUB_TASKS("sub_tasks", "task_id"),
        TEAM_SUB_TASKS("team_sub_tasks", "team_task_id");

        private final String tableName;
        private final String foreignKeyColumn;

        TaskType(String tableName, String foreignKeyColumn) {
            this.tableName = tableName;
            this.foreignKeyColumn = foreignKeyColumn;
        }

        public String getTableName() {
            return tableName;
        }

        public String getForeignKeyColumn() {
            return foreignKeyColumn;
        }
    }

    private String INSERT_SUB_TASK(TaskType taskType) {
        return String.format("""
                insert into %s (%s, task, created_at, updated_at)
                values ($1, $2, $3, $4)
                returning id
                """, taskType.getTableName(), taskType.getForeignKeyColumn());
    }

    private String UPDATE_STATUS_SUB_TASK(TaskType taskType) {
        return String.format("""
                update %s set isdone = true, updated_at = $1 where id = $2
                """, taskType.getTableName());
    }

    private String DELETE_SUB_TASK(TaskType taskType) {
        return String.format("""
                delete from %s where id = $1
                """, taskType.getTableName());
    }

    private String GET_ALL_SUB_TASKS(TaskType taskType) {
        return String.format("""
                select id, %s, task, isdone, created_at, updated_at from %s where %s = $1
                """, taskType.getForeignKeyColumn(), taskType.getTableName(), taskType.getForeignKeyColumn());
    }

    private String GET_SUB_TASK_BY_ID(TaskType taskType) {
        return String.format("""
                select id, %s, task, isdone, created_at, updated_at from %s where id = $1
                """, taskType.getForeignKeyColumn(), taskType.getTableName());
    }

    private SubTaskEntity mapping(Row row, TaskType taskType) {
        return new SubTaskEntity(
                row.getInteger(taskType.getForeignKeyColumn()),
                row.getInteger("id"),
                row.getString("task"),
                row.getBoolean("isdone"),
                row.getLocalDateTime("created_at"),
                row.getLocalDateTime("updated_at"));
    }

    // Insert with connection
    public Uni<Integer> insert(SubTaskEntity request, TaskType taskType, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        return conn.preparedQuery(INSERT_SUB_TASK(taskType))
                .execute(Tuple.of(
                        request.getTaskId(),
                        request.getTask(),
                        now,
                        now))
                .map(rows -> rows.iterator().next().getInteger("id"))
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to insert %s : %s", taskType.getTableName(), throwable.getMessage());
                });
    }

    // Insert without connection
    public Uni<Integer> insert(SubTaskEntity request, TaskType taskType) {
        return client.withConnection(conn -> insert(request, taskType, conn));
    }

    // Update status with connection
    public Uni<Void> updateStatus(Integer id, TaskType taskType, SqlConnection conn) {
        return conn.preparedQuery(UPDATE_STATUS_SUB_TASK(taskType))
                .execute(Tuple.of(LocalDateTime.now(), id))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to update status %s [ID:%s] : %s", taskType.getTableName(), id,
                            throwable.getMessage());
                });
    }

    // Update status without connection
    public Uni<Void> updateStatus(Integer id, TaskType taskType) {
        return client.withConnection(conn -> updateStatus(id, taskType, conn));
    }

    // Delete with connection
    public Uni<Void> delete(Integer id, TaskType taskType, SqlConnection conn) {
        return conn.preparedQuery(DELETE_SUB_TASK(taskType))
                .execute(Tuple.of(id))
                .replaceWithVoid()
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to delete %s [ID:%s] : %s", taskType.getTableName(), id, throwable.getMessage());
                });
    }

    // Delete without connection
    public Uni<Void> delete(Integer id, TaskType taskType) {
        return client.withConnection(conn -> delete(id, taskType, conn));
    }

    // Get all with connection
    public Uni<List<SubTaskEntity>> getAll(Integer taskId, TaskType taskType, SqlConnection conn) {
        return conn.preparedQuery(GET_ALL_SUB_TASKS(taskType))
                .execute(Tuple.of(taskId))
                .map(rows -> {
                    List<SubTaskEntity> tasks = new ArrayList<>();
                    for (Row row : rows) {
                        tasks.add(mapping(row, taskType));
                    }
                    return tasks;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get %s [TaskID:%s] : %s", taskType.getTableName(), taskId,
                            throwable.getMessage());
                });
    }

    // Get all without connection
    public Uni<List<SubTaskEntity>> getAll(Integer taskId, TaskType taskType) {
        return client.withConnection(conn -> getAll(taskId, taskType, conn));
    }

    // Get by ID with connection
    public Uni<SubTaskEntity> getById(Integer id, TaskType taskType, SqlConnection conn) {
        return conn.preparedQuery(GET_SUB_TASK_BY_ID(taskType))
                .execute(Tuple.of(id))
                .map(rows -> {
                    if (rows.size() == 0) {
                        return null;
                    }
                    return mapping(rows.iterator().next(), taskType);
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get %s [ID:%s] : %s", taskType.getTableName(), id, throwable.getMessage());
                });
    }

    // Get by ID without connection
    public Uni<SubTaskEntity> getById(Integer id, TaskType taskType) {
        return client.withConnection(conn -> getById(id, taskType, conn));
    }
}
