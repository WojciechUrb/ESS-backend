package com.urbaniak.evacuation_support_system.repositories;

import com.urbaniak.evacuation_support_system.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByUuid(UUID uuid);

    @Query("SELECT u.fcmToken FROM UserModel u WHERE " +
            " u.roomId IS NOT NULL" +
            " AND u.fcmToken IS NOT NULL" +
            " AND u.localizationUpdatedAt > :sinceTime")
    List<String> findAllTokens(@Param("sinceTime") Instant sinceTime);

    @Modifying
    @Query("UPDATE UserModel u SET u.fcmToken = null WHERE u.fcmToken = :token AND u.uuid <> :uuid")
    void clearToken(@Param("token") String token, @Param("uuid") UUID uuid);

}
