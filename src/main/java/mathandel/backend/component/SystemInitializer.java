package mathandel.backend.component;

import mathandel.backend.exception.AppException;
import mathandel.backend.model.server.EditionStatusType;
import mathandel.backend.model.server.ModeratorRequestStatus;
import mathandel.backend.model.server.Role;
import mathandel.backend.model.server.User;
import mathandel.backend.model.server.enums.EditionStatusName;
import mathandel.backend.model.server.enums.ModeratorRequestStatusName;
import mathandel.backend.model.server.enums.RoleName;
import mathandel.backend.repository.EditionStatusTypeRepository;
import mathandel.backend.repository.ModeratorRequestStatusRepository;
import mathandel.backend.repository.RoleRepository;
import mathandel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class SystemInitializer implements ApplicationRunner {

    @Value("${populate.mathandel.data}")
    private Boolean populateMathandelData;

    @Value("${populate.mathandel.data.file.path}")
    private String populateMathandelDataFilePath;

    @Value("${populate.mathandel.items.data.file.path}")
    private String populateMathandelItemsDataFilePath;

    @Value("${populate.mathandel.items.with.urls}")
    private String populateMathandelImageUrlsWithIds;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EditionStatusTypeRepository editionStatusTypeRepository;
    private final ModeratorRequestStatusRepository moderatorRequestStatusRepository;
    private final MathandelDataPopulator mathandelDataPopulator;


    public SystemInitializer(RoleRepository roleRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             EditionStatusTypeRepository editionStatusTypeRepository,
                             ModeratorRequestStatusRepository moderatorRequestStatusRepository,
                             MathandelDataPopulator mathandelDataPopulator) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.editionStatusTypeRepository = editionStatusTypeRepository;
        this.moderatorRequestStatusRepository = moderatorRequestStatusRepository;
        this.mathandelDataPopulator = mathandelDataPopulator;
    }

    @Override
    public void run(ApplicationArguments args) {
        insertRolesToDB();
        insertEditionStatusesToDB();
        insertAdminToDB();
        insertModeratorRequestStatusesToDB();

        if (populateMathandelData) {
            try {
                mathandelDataPopulator.saveItemsFromFile(populateMathandelItemsDataFilePath);
                long lStartTime = System.nanoTime();

                mathandelDataPopulator.saveImagesFromUrls(populateMathandelImageUrlsWithIds);

                mathandelDataPopulator.saveFromFile(populateMathandelDataFilePath);
                long lEndTime = System.nanoTime();
                long output = lEndTime - lStartTime;
                System.out.println("POPULATOR TERMINATED WITH TIME -- " + output / 1000000000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void insertRolesToDB() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void insertEditionStatusesToDB() {
        for (EditionStatusName editionStatusName : EditionStatusName.values()) {
            if (!editionStatusTypeRepository.existsByEditionStatusName(editionStatusName)) {
                EditionStatusType editionStatusType = new EditionStatusType();
                editionStatusType.setEditionStatusName(editionStatusName);
                editionStatusTypeRepository.save(editionStatusType);
            }
        }
    }

    private void insertAdminToDB() {
        if (!(userRepository.existsById(1L) || userRepository.existsByUsername("admin") || userRepository.existsByEmail("admin@admin.admin"))) {
            User user = new User()
                    .setName("admin")
                    .setSurname("admin")
                    .setUsername("admin")
                    .setEmail("admin@admin.admin")
                    .setPassword(passwordEncoder.encode("admin"))
                    .setAddress("admin")
                    .setCity("admin")
                    .setPostalCode("admin")
                    .setCountry("admin");

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
        }
    }

    private void insertModeratorRequestStatusesToDB() {
        for (ModeratorRequestStatusName moderatorRequestStatusName : ModeratorRequestStatusName.values()) {
            if (!moderatorRequestStatusRepository.existsByName(moderatorRequestStatusName)) {
                moderatorRequestStatusRepository.save(new ModeratorRequestStatus().setName(moderatorRequestStatusName));
            }
        }
    }
}

