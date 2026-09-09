package com.mavora.identity.domain;

import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String normalizedEmail);

    boolean existsByEmail(String normalizedEmail);
}
