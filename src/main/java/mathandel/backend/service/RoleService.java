package mathandel.backend.service;

import mathandel.backend.model.client.response.ApiResponse;
import mathandel.backend.exception.AppException;
import mathandel.backend.exception.BadRequestException;
import mathandel.backend.model.client.ModeratorRequestTO;
import mathandel.backend.model.server.ModeratorRequest;
import mathandel.backend.model.server.ModeratorRequestStatus;
import mathandel.backend.model.server.ModeratorRequestStatusName;
import mathandel.backend.model.server.User;
import mathandel.backend.model.server.enums.RoleName;
import mathandel.backend.repository.ModeratorRequestsRepository;
import mathandel.backend.repository.UserRepository;
import mathandel.backend.utils.ServerToClientDataConverter;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static mathandel.backend.utils.ServerToClientDataConverter.mapModeratorRequest;

@Service
public class RoleService {

    private final ModeratorRequestsRepository moderatorRequestsRepository;
    private final UserRepository userRepository;

    public RoleService(ModeratorRequestsRepository moderatorRequestsRepository, UserRepository userRepository) {
        this.moderatorRequestsRepository = moderatorRequestsRepository;
        this.userRepository = userRepository;
    }

    public ApiResponse requestModerator(@Valid ModeratorRequestTO moderatorRequestTO, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException("User doesn't exist"));

        if(moderatorRequestsRepository.existsByUser(user)) {
            throw new BadRequestException("Request already submitted");
        }

        ModeratorRequest moderatorRequest = new ModeratorRequest().setUser(user)
                .setReason(moderatorRequestTO.getReason())
                .setModeratorRequestStatus(new ModeratorRequestStatus().setName(ModeratorRequestStatusName.PENDING));

        moderatorRequestsRepository.save(moderatorRequest);

        return new ApiResponse("Request submitted");
    }

    // todo mapRequests static method
    public List<ModeratorRequestTO> getModeratorRequests() {
        return moderatorRequestsRepository.findAllByModeratorRequestStatus_Name(ModeratorRequestStatusName.PENDING).stream().map(ServerToClientDataConverter::mapModeratorRequest).collect(Collectors.toList());
    }

    public ApiResponse resolveModeratorRequests(List<ModeratorRequestTO> resolvedRequests) {
        ModeratorRequest moderatorRequest;

        for (ModeratorRequestTO resolvedRequest : resolvedRequests) {
            //todo findByModeratorRequest_Id
            moderatorRequest = moderatorRequestsRepository.findModeratorRequestsByUser_Id(resolvedRequest.getUserId())
                    .orElseThrow(() -> new AppException("No entry in moderator_requests for user " + resolvedRequest.getUserId()));

            moderatorRequest
                    .getModeratorRequestStatus()
                    .setName(resolvedRequest.getModeratorRequestStatus());

            moderatorRequestsRepository.save(moderatorRequest);
        }

        return new ApiResponse("Requests resolved");
    }

    public ModeratorRequestTO getUserRequests(Long userId) {
        ModeratorRequest moderatorRequest =
                moderatorRequestsRepository
                        .findModeratorRequestsByUser_Id(userId)
                        .orElseThrow(() -> new AppException("No entry in moderator_requests for user " + userId));

        return mapModeratorRequest(moderatorRequest);
    }

    private boolean hasRole(RoleName roleName, User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }
}
