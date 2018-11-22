package mathandel.backend.service;

import mathandel.backend.exception.AppException;
import mathandel.backend.exception.BadRequestException;
import mathandel.backend.exception.ResourceNotFoundException;
import mathandel.backend.model.client.EditionTO;
import mathandel.backend.model.client.response.ApiResponse;
import mathandel.backend.model.server.Edition;
import mathandel.backend.model.server.EditionStatusType;
import mathandel.backend.model.server.Role;
import mathandel.backend.model.server.User;
import mathandel.backend.model.server.enums.EditionStatusName;
import mathandel.backend.repository.EditionRepository;
import mathandel.backend.repository.EditionStatusTypeRepository;
import mathandel.backend.repository.RoleRepository;
import mathandel.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mathandel.backend.model.server.enums.RoleName.ROLE_ADMIN;
import static mathandel.backend.model.server.enums.RoleName.ROLE_MODERATOR;
import static mathandel.backend.utils.ServerToClientDataConverter.mapEdition;
import static mathandel.backend.utils.ServerToClientDataConverter.mapEditions;

//todo it tests
@Service
public class EditionService {

    private final EditionRepository editionRepository;
    private final EditionStatusTypeRepository editionStatusTypeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public EditionService(EditionRepository editionRepository, EditionStatusTypeRepository editionStatusTypeRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.editionRepository = editionRepository;
        this.editionStatusTypeRepository = editionStatusTypeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public EditionTO createEdition(EditionTO editionTO, Long userId) {

        validateEdition(editionTO);
        if (editionTO.getMaxParticipants() < 1) {
            throw new BadRequestException("Incorrect max participants value - has to be more than 0");
        }

        Role adminRole = roleRepository.findByName(ROLE_ADMIN).orElseThrow(() -> new AppException("Admin Role does not exist"));
        User admin = userRepository.findByRolesContains(adminRole).orElseThrow(() -> new AppException("Admin does not exist"));
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("User doesn't exist"));

        Set<User> moderators = new HashSet<>();
        Set<User> participants = new HashSet<>();
        moderators.add(user);
        participants.add(user);

        if (!user.getId().equals(admin.getId())) {
            moderators.add(admin);
            participants.add(admin);
        }

        EditionStatusType editionStatusType = editionStatusTypeRepository.findByEditionStatusName(EditionStatusName.OPENED);
        Edition edition = new Edition()
                .setName(editionTO.getName())
                .setEndDate(editionTO.getEndDate())
                .setModerators(moderators)
                .setParticipants(participants)
                .setEditionStatusType(editionStatusType)
                .setDescription(editionTO.getDescription())
                .setMaxParticipants(editionTO.getMaxParticipants());

        return mapEdition(editionRepository.save(edition), userId);
    }

    private void validateEdition(EditionTO editionTO) {
        if (editionRepository.existsByName(editionTO.getName())) {
            throw new BadRequestException("Edition name already exists");
        }
        if (editionTO.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Edition end date cannot be in the past");
        }
    }

    public EditionTO editEdition(EditionTO editionTO, Long editionId, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("User doesn't exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));

        if (!edition.getModerators().contains(user)) {
            throw new BadRequestException("You are not moderator of this edition");
        }
        validateEdition(editionTO);
        if (editionTO.getMaxParticipants() < edition.getParticipants().size()) {
            throw new BadRequestException("Cannot lower max number of participants");
        }

        edition.setName(editionTO.getName());
        edition.setEndDate(editionTO.getEndDate());
        edition.setDescription(editionTO.getDescription());
        edition.setMaxParticipants(editionTO.getMaxParticipants());

        return mapEdition(editionRepository.save(edition), userId);
    }

    public List<EditionTO> getEditions(Long userId) {
        return mapEditions(editionRepository.findAll(), userId);
    }

    public ApiResponse makeUserEditionModerator(Long userId, Long editionId, String username) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));
        User requestedUser = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!edition.getEditionStatusType().getEditionStatusName().equals(EditionStatusName.OPENED)) {
            throw new BadRequestException("Edition is not opened");
        }
        if (!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }
        if (!isModerator(requestedUser)) {
            throw new BadRequestException("Requested user is not moderator");
        }
        if(edition.getParticipants().size() == edition.getMaxParticipants() && !edition.getParticipants().contains(requestedUser)) {
            throw new BadRequestException("Requested moderator is not participant of this edition and this edition already has max participants");
        }

        edition.getModerators().add(requestedUser);
        edition.getParticipants().add(requestedUser);
        editionRepository.save(edition);
        return new ApiResponse("User " + username + " become moderator of edition " + edition.getName());
    }

    private boolean isModerator(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(ROLE_MODERATOR));
    }

    void changeEditionStatus(Long userId, Long editionId, EditionStatusName editionStatusName) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));

        if(!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }

        edition.setEditionStatusType(editionStatusTypeRepository.findByEditionStatusName(editionStatusName));

        editionRepository.save(edition);
    }
}
