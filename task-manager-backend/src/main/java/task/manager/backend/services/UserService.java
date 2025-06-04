package task.manager.backend.services;

import org.mindrot.jbcrypt.BCrypt;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import task.manager.backend.dto.request.UserRequest;
import task.manager.backend.dto.response.UserResponse;
import task.manager.backend.entity.UserEntity;
import task.manager.backend.repositories.UserRepository;
import task.manager.backend.utils.JWTUtils;

@ApplicationScoped
public class UserService {
    @Inject
    UserRepository userRepository;

    @Inject
    JWTUtils jwtUtils;

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private Boolean comparePassword(String password, String hashPassword) {
        return BCrypt.checkpw(password, hashPassword);
    }

    public Uni<Void> registerUser(
            @Valid UserRequest.register request) {
        return Uni.createFrom().item(() -> {
            return hashPassword(request.password());
        }).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .flatMap(hashedPassword -> {
                    UserEntity user = new UserEntity();
                    user.setUsername(request.username());
                    user.setPassword(hashedPassword);

                    return userRepository.insert(user);
                })
                .replaceWithVoid()
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to create user: " + throwable.getMessage(), throwable);
                });
    }

    public Uni<UserResponse.login> loginUser(@Valid UserRequest.login request) {
        return userRepository.get(request.username())
                .onItem().ifNull().failWith(() -> new RuntimeException("User not found"))
                .flatMap(user -> Uni.createFrom().item(() -> {
                    if (!comparePassword(request.password(), user.getPassword())) {
                        throw new RuntimeException("Invalid username or password");
                    }
                    return user;
                })
                        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool()))
                .map(user -> {
                    String token = jwtUtils.generateToken(user);
                    return new UserResponse.login(user.getUsername(), token);
                })
                .onFailure().transform(throwable -> {
                    return new RuntimeException("Failed to login user: " + throwable.getMessage(), throwable);
                });
    }

}
