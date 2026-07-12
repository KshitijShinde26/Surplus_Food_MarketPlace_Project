package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Query(value = "SELECT u.*, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:longitude)) + sin(radians(:latitude)) * sin(radians(u.latitude)))) AS distance " +
            "FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name IN ('ROLE_CONSUMER', 'ROLE_NGO') " +
            "HAVING distance <= :radius", nativeQuery = true)
    java.util.List<User> findNearbyConsumersAndNgos(
            @org.springframework.data.repository.query.Param("latitude") double latitude,
            @org.springframework.data.repository.query.Param("longitude") double longitude,
            @org.springframework.data.repository.query.Param("radius") double radius
    );

    @org.springframework.data.jpa.repository.Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    java.util.List<Object[]> countUsersByRole();
}
