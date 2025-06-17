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
import task.manager.backend.dto.response.ProjectMemberResponse;
import task.manager.backend.entity.ProjectMemberEntity;

@ApplicationScoped
public class ProjectMemberRepository {
    private static final Logger log = Logger.getLogger(ProjectMemberRepository.class);

    @Inject
    Pool client;

    private static final String INSERT_MEMBER = """
            insert into project_members(project_id, user_id, role_id, joined_at)
            values ($1,$2, $3, $4)
            returning id
            """;

    private static final String GET_BY_PROJECT_ID_USER_ID = """
            select p.id, p.project_id, p.user_id, p.role_id, p.joined_at, p.left_at, p.status, r.name, r.level, r.description, u.username
            from project_members p
            inner join roles r on p.role_id = r.id
            inner join users u on p.user_id = u.id
            where p.project_id = $1 and p.user_id = $2
            """;

    private static final String GET_MEMBERS_PROJECT = """
            select p.id, p.project_id, p.user_id, p.role_id, p.joined_at, p.left_at, p.status, r.name, r.level, r.description, u.username
            from project_members p
            inner join roles r on p.role_id = r.id
            inner join users u on p.user_id = u.id
            where p.project_id = $1
            """;

    private ProjectMemberResponse.member mappingResult(Row row) {
        return new ProjectMemberResponse.member(
                row.getInteger("id"),
                row.getString("username"),
                row.getInteger("project_id"),
                row.getInteger("user_id"),
                row.getInteger("role_id"),
                row.getLocalDateTime("joined_at"),
                row.getLocalDateTime("left_at"),
                row.getString("status"),
                row.getInteger("level"),
                row.getString("description"));
    }

    public Uni<Integer> insert(ProjectMemberEntity member, SqlConnection conn) {
        LocalDateTime now = LocalDateTime.now();
        member.setJoinedAt(now);

        return conn.preparedQuery(INSERT_MEMBER)
                .execute(Tuple.of(member.getProjectId(), member.getUserId(), member.getRoleId(), member.getJoinedAt()))
                .onItem().transform(rows -> {
                    return rows.iterator().next().getInteger("id");
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to insert project member with [ID:%d]: %s", member.getUserId(), throwable);
                });
    }

    public Uni<Integer> insert(ProjectMemberEntity member) {
        return client.withConnection(conn -> insert(member, conn));
    }

    public Uni<ProjectMemberResponse.member> getByUserIDAndProjectID(Integer projectId, Integer userId,
            SqlConnection conn) {
        return conn.preparedQuery(GET_BY_PROJECT_ID_USER_ID)
                .execute(Tuple.of(projectId, userId))
                .onItem().transform(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? mappingResult(iterator.next()) : null;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get project member with [ID:%d and ProjectID:%d]: %s", userId, projectId,
                            throwable);
                });
    }

    public Uni<ProjectMemberResponse.member> getByUserIDAndProjectID(Integer projectId, Integer userId) {
        return client.withConnection(conn -> getByUserIDAndProjectID(projectId, userId, conn));
    }

    public Uni<List<ProjectMemberResponse.member>> getMembers(Integer projectId, SqlConnection conn) {
        return conn.preparedQuery(GET_MEMBERS_PROJECT)
                .execute(Tuple.of(projectId))
                .onItem().transform(rows -> {
                    List<ProjectMemberResponse.member> members = new ArrayList<>();
                    rows.forEach(row -> members.add(mappingResult(row)));
                    return members;
                })
                .onFailure().invoke(throwable -> {
                    log.errorf("failed to get project members with [ProjectID:%d]: %s", projectId,
                            throwable);
                });
    }

    public Uni<List<ProjectMemberResponse.member>> getMembers(Integer projectId) {
        return client.withConnection(conn -> getMembers(projectId, conn));
    }
}
