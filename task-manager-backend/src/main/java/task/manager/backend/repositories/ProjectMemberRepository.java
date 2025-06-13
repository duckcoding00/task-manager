package task.manager.backend.repositories;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
}
