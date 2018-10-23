package mathandel.backend.service;

import mathandel.backend.client.response.ApiResponse;
import mathandel.backend.exception.BadRequestException;
import mathandel.backend.exception.ResourceNotFoundException;
import mathandel.backend.model.client.PreferenceTO;
import mathandel.backend.model.server.Preference;
import mathandel.backend.model.server.Product;
import mathandel.backend.repository.*;
import mathandel.backend.utils.ServerToClientDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PreferenceService {

    private ProductRepository productRepository;

    private PreferenceRepository preferenceRepository;

    private EditionRepository editionRepository;

    private UserRepository userRepository;

    private DefinedGroupRepository definedGroupRepository;

    @Autowired
    public PreferenceService(ProductRepository productRepository, PreferenceRepository preferenceRepository, EditionRepository editionRepository, UserRepository userRepository, DefinedGroupRepository definedGroupRepository) {
        this.productRepository = productRepository;
        this.preferenceRepository = preferenceRepository;
        this.editionRepository = editionRepository;
        this.userRepository = userRepository;
        this.definedGroupRepository = definedGroupRepository;
    }


    public ApiResponse addEditPreference(Long userId, Long haveProductId, Set<Long> wantProductIds, Long editionId) {

        Product haveProduct = productRepository.findById(haveProductId).orElseThrow(() -> new ResourceNotFoundException("Product", "id", haveProductId));

        if (!haveProduct.getUser().getId().equals(userId)) {
            throw new BadRequestException("User is not an owner of the first role");
        }


        Preference preference;
        if (preferenceRepository.findByHaveProduct_Id(haveProductId) == null) {
            preference = new Preference()
                    .setHaveProduct(haveProduct)
                    .setUser(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)))
                    .setEdition(editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId)));


        } else {
            preference = preferenceRepository.findByHaveProduct_Id(haveProductId);
        }

        for (Long wantProductId : wantProductIds) {
            preference.getWantedProducts().add(productRepository.findById(wantProductId).orElseThrow(() -> new ResourceNotFoundException("Product", "id", wantProductId)));
        }


        preferenceRepository.save(preference);

        return new ApiResponse("Preference saved");

    }

    public ApiResponse addEditGroupPreference(Long userId, Long haveProductId, Set<Long> wantGroupIds, Long editionId) {

        Product haveProduct = productRepository.findById(haveProductId).orElseThrow(() -> new ResourceNotFoundException("Product", "id", haveProductId));

        if (!haveProduct.getUser().getId().equals(userId)) {
            throw new BadRequestException("User is not an owner of the first role");
        }

        Preference preference;
        if (preferenceRepository.findByHaveProduct_Id(haveProductId) == null) {
            preference = new Preference()
                    .setHaveProduct(haveProduct)
                    .setUser(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)))
                    .setEdition(editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId)));


        } else {
            preference = preferenceRepository.findByHaveProduct_Id(haveProductId);
        }

        for (Long wantGroupId : wantGroupIds) {
            preference.getWantedDefinedGroups().add(definedGroupRepository.findById(wantGroupId).orElseThrow(() -> new ResourceNotFoundException("Group", "id", wantGroupId)));
        }

        preferenceRepository.save(preference);

        return new ApiResponse("Preference saved");
    }

    public Set<PreferenceTO> getUserPreferencesFromOneEdtion(Long userId, Long editionId) {
        return preferenceRepository.findAllByUser_IdAndEdition_Id(userId, editionId).stream().map(ServerToClientDataConverter::mapPreference).collect(Collectors.toSet());

    }

}
