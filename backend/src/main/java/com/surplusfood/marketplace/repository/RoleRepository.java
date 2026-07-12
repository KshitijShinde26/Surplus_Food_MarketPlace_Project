package com.surplusfood.marketplace.repository;

import com.surplusfood.marketplace.entity.Role;
import com.surplusfood.marketplace.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
