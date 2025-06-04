package task.manager.backend.repositories;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlConnection;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import task.manager.backend.entity.UserEntity;

@ApplicationScoped
public class UserRepository {
    private static final Logger log = Logger.getLogger(UserRepository.class);

    @Inject
    Pool client;

    public Uni<UserEntity> insert(UserEntity user, SqlConnection conn) {
        return conn.preparedQuery("""
                insert into users(username, password)
                values($1,$2)
                """).execute(
                Tuple.tuple()
                        .addValue(user.getUsername())
                        .addValue(user.getPassword()))
                .onItem().transform(ignore -> user)
                .onFailure().invoke(throwable -> {
                    log.error("failed insert user : ", throwable);
                });
    }

    // transaction reactive
    public Uni<UserEntity> insert(UserEntity user) {
        return client.withConnection(conn -> insert(user, conn));
    }

    public Uni<UserEntity> get(String username, SqlConnection conn) {
        return conn.preparedQuery("""
                select id, username, password from users
                where username = $1
                """)
                .execute(Tuple.of(username))
                .onItem().transform(rows -> {
                    var iterator = rows.iterator();
                    if (iterator.hasNext()) {
                        var row = iterator.next();
                        UserEntity user = new UserEntity();
                        user.setId(row.getInteger("id"));
                        user.setUsername(row.getString("username"));
                        user.setPassword(row.getString("password"));

                        return user;
                    }

                    return null;
                })
                .onFailure().invoke(throwable -> {
                    log.error("failed get user : ", throwable);
                });
    }

    public Uni<UserEntity> get(String username) {
        return client.withConnection(conn -> get(username, conn));
    }
}
