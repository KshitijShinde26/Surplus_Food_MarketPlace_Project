package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.RefreshToken;
import com.surplusfood.marketplace.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUser(User user);

    @Modifying
    void deleteByToken(String token);

    @Modifying
    void deleteByExpiresAtBefore(Instant cutoff);
}
