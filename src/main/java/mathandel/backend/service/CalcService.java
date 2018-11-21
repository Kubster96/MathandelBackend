package mathandel.backend.service;

import mathandel.backend.exception.AppException;
import mathandel.backend.exception.ResourceNotFoundException;
import mathandel.backend.model.client.response.ApiResponse;
import mathandel.backend.model.server.*;
import mathandel.backend.model.server.enums.EditionStatusName;
import mathandel.backend.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//todo review and refactor
@Service
public class CalcService {

    private PreferenceRepository preferenceRepository;
    private DefinedGroupRepository definedGroupRepository;
    private ResultRepository resultRepository;
    private ItemRepository itemRepository;
    private EditionRepository editionRepository;
    private final EditionService editionService;
    private final RestTemplate restTemplate;

    @Value("${calc.service.url}")
    private String CALC_SERVICE_URL;

    public CalcService(PreferenceRepository preferenceRepository, DefinedGroupRepository definedGroupRepository, ResultRepository resultRepository, ItemRepository itemRepository, EditionRepository editionRepository, EditionService editionService, RestTemplate restTemplate) {
        this.preferenceRepository = preferenceRepository;
        this.definedGroupRepository = definedGroupRepository;
        this.resultRepository = resultRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
        this.editionService = editionService;
        this.restTemplate = restTemplate;
    }

    // todo test this
    public ApiResponse closeEdition(Long userId, Long editionId) {

        Edition initialEdition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));
        EditionStatusName initialEditionStatus = initialEdition.getEditionStatusType().getEditionStatusName();
        editionService.changeEditionStatus(userId, editionId, EditionStatusName.CLOSED);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        String body = getMappedDataForEdition(editionId);
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            String result = restTemplate.postForObject(CALC_SERVICE_URL + "/solve/", httpEntity, String.class);

            saveResultsFromJsonData(editionId, result);

            editionService.changeEditionStatus(userId, editionId, EditionStatusName.FINISHED);
            nullEditionIdsOfAllItemsThatWerentChosen(editionId);

            return new ApiResponse("Edition closed, you can now check for results");
        } catch (Exception e) {
            editionService.changeEditionStatus(userId,editionId, initialEditionStatus);
            throw new AppException("Server had a problem with calculating result for your edition. Try again later.");
        }

    }

    private void saveResultsFromJsonData(Long editionId, String jsonString) {
        JSONArray jsonData = new JSONArray(jsonString);

        for (int i = 0; i < jsonData.length(); i++) {
            JSONObject node = jsonData.getJSONObject(i);

            resultRepository.save(getResult(editionId, node));
        }
    }

    private Result getResult(Long editionId, JSONObject node) {
        Long receiversItemId = node.getLong("receiver");
        Long itemToSentId = node.getLong("sender");

        Item itemToSent = itemRepository.findById(itemToSentId).orElseThrow(() -> new AppException("Item not found, probably the calculation data have been corrupted"));
        Item receiversItem = itemRepository.findById(receiversItemId).orElseThrow(() -> new AppException("Item not found,probably the calculation data have been corrupted"));

        User receiver = receiversItem.getUser();
        User sender = itemToSent.getUser();

        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new AppException("Edition not found"));

        return new Result()
                .setEdition(edition)
                .setItemToSend(itemToSent)
                .setReceiver(receiver)
                .setSender(sender);
    }

    private String getMappedDataForEdition(Long editionId) {
        Set<Preference> preferences = preferenceRepository.findAllByEdition_Id(editionId);
        Set<DefinedGroup> definedGroups = definedGroupRepository.findAllByEdition_Id(editionId);

        JSONObject mappedData = new JSONObject();
        JSONArray definedGroupsAsJsonTable = new JSONArray(definedGroups.stream().map(this::mapGroup).collect(Collectors.toList()));
        JSONArray preferencesAsJsonTable = new JSONArray(preferences.stream().map(this::mapPreference).collect(Collectors.toList()));

        return mappedData
                .put("named_groups", definedGroupsAsJsonTable)
                .put("preferences", preferencesAsJsonTable).toString();
    }

    private JSONObject mapGroup(DefinedGroup definedGroup) {
        return new JSONObject()
                .put("id", definedGroup.getId())
                .put("single_preferences", new JSONArray(definedGroup.getItems().stream().map(Item::getId).collect(Collectors.toList())))
                .put("groups", new JSONArray(definedGroup.getGroups().stream().map(DefinedGroup::getId).collect(Collectors.toList())));
    }


    private JSONObject mapPreference(Preference preference) {
        return new JSONObject()
                .put("id", preference.getHaveItem().getId())
                .put("single_preferences", new JSONArray(preference.getWantedItems().stream().map(Item::getId).collect(Collectors.toList())))
                .put("groups", new JSONArray(preference.getWantedDefinedGroups().stream().map(DefinedGroup::getId).collect(Collectors.toList())));
    }

    private void nullEditionIdsOfAllItemsThatWerentChosen(Long editionId) {
        Set<Long> resultItemsIds = resultRepository.findAllByEdition_Id(editionId).stream().map(r -> r.getItemToSend().getId()).collect(Collectors.toSet());
        Set<Item> editionItems = itemRepository.findAllByEdition_Id(editionId);
        Set<Item> notAssignedItems = new HashSet<>();

        for (Item p : editionItems) {

            if (!resultItemsIds.contains(p.getId())) {
                p.setEdition(null);
                notAssignedItems.add(p);
            }
        }

        itemRepository.saveAll(notAssignedItems);
    }
}
