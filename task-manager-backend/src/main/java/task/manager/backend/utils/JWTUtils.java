package task.manager.backend.utils;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;
import task.manager.backend.entity.UserEntity;

@Singleton
public class JWTUtils {
    public String generateToken(UserEntity user) {
        HashSet<String> groups = new HashSet<>(
                Arrays.asList("user"));

        return Jwt.issuer("task.manager.app")
                .subject(user.getUsername())
                .groups(groups)
                .claim("userId", user.getId())
                .expiresIn(Duration.ofMinutes(60 * 24 * 30))
                .sign();
    }

    public JWTClaimsSet decode(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }
}
