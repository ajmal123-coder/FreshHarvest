package com.Fresh_harvest.Backend.repository;

import com.Fresh_harvest.Backend.model.ERole;
import com.Fresh_harvest.Backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
