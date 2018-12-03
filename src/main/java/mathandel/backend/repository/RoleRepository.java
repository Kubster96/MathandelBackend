package mathandel.backend.repository;

import mathandel.backend.model.server.Role;
import mathandel.backend.model.server.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
    Boolean existsByName(RoleName name);
}
