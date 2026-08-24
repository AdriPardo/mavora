package com.mavora.identity.infrastructure.persistence;

import com.mavora.identity.domain.Email;
import com.mavora.identity.domain.User;
import com.mavora.identity.domain.UserRepository;
import com.mavora.shared.domain.UserId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaUserRepository implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public JpaUserRepository(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        entity.markNew(!jpaRepository.existsById(user.id().value()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String normalizedEmail) {
        return jpaRepository.findByEmail(normalizedEmail).map(JpaUserRepository::toDomain);
    }

    @Override
    public boolean existsByEmail(String normalizedEmail) {
        return jpaRepository.existsByEmail(normalizedEmail);
    }

    static User toDomain(UserEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id().value());
        entity.setEmail(user.email().normalized());
        entity.setPasswordHash(user.passwordHash());
        entity.setStatus(user.status());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        entity.setVersion(user.version());
        return entity;
    }
}
