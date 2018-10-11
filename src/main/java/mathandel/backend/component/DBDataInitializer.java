package mathandel.backend.component;

import mathandel.backend.exception.AppException;
import mathandel.backend.model.*;
import mathandel.backend.model.enums.EditionStatusName;
import mathandel.backend.model.enums.RoleName;
import mathandel.backend.repository.EditionStatusTypeRepository;
import mathandel.backend.repository.RoleRepository;
import mathandel.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DBDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EditionStatusTypeRepository editionStatusTypeRepository;

    public DBDataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, EditionStatusTypeRepository editionStatusTypeRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.editionStatusTypeRepository = editionStatusTypeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        insertRolesToDB();
        insertEditionStatusesToDB();
        insertAdminToDB();
    }

    private void insertRolesToDB() {
        for (RoleName roleName : RoleName.values()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
        }
    }

    private void insertEditionStatusesToDB() {
        for (EditionStatusName editionStatusName : EditionStatusName.values()) {
            EditionStatusType editionStatusType = new EditionStatusType();
            editionStatusType.setEditionStatusName(editionStatusName);
            editionStatusTypeRepository.save(editionStatusType);
        }
    }

    private void insertAdminToDB() {
        User user = new User("admin", "admin", "admin", "admin@admin.admin", "admin");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("Admin Role not set."));
        Role moderatorRole = roleRepository.findByName(RoleName.ROLE_MODERATOR)
                .orElseThrow(() -> new AppException("Moderator Role not set."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        roles.add(moderatorRole);
        user.setRoles(roles);

        userRepository.save(user);

        // todo zaladowac requesty
    }
}

