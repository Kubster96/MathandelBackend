package mathandel.backend.service;

import mathandel.backend.exception.AppException;
import mathandel.backend.model.server.*;
import mathandel.backend.repository.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.stream.Collectors;

import static mathandel.backend.model.server.enums.EditionStatusName.CLOSED;
import static mathandel.backend.model.server.enums.EditionStatusName.FAILED;

public class ResultCalculator implements Runnable {

    private ResultRepository resultRepository;
    private String CALC_SERVICE_TOKEN;
    private RestTemplate restTemplate;
    private Edition edition;
    private String CALC_SERVICE_URL;
    private EditionService editionService;
    private PreferenceRepository preferenceRepository;
    private DefinedGroupRepository definedGroupRepository;
    private ItemRepository itemRepository;
    private EditionRepository editionRepository;

    public ResultCalculator(ResultRepository resultRepository,
                            String CALC_SERVICE_TOKEN,
                            RestTemplate restTemplate,
                            Edition edition,
                            String CALC_SERVICE_URL,
                            EditionService editionService,
                            PreferenceRepository preferenceRepository,
                            DefinedGroupRepository definedGroupRepository,
                            ItemRepository itemRepository,
                            EditionRepository editionRepository) {
        this.resultRepository = resultRepository;
        this.CALC_SERVICE_TOKEN = CALC_SERVICE_TOKEN;
        this.restTemplate = restTemplate;
        this.edition = edition;
        this.CALC_SERVICE_URL = CALC_SERVICE_URL;
        this.editionService = editionService;
        this.preferenceRepository = preferenceRepository;
        this.definedGroupRepository = definedGroupRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
    }

    @Override
    public void run() {
        resultRepository.deleteAllByEdition_Id(edition.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", CALC_SERVICE_TOKEN);
        String body = getMappedDataForEdition(edition.getId());
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            String result = restTemplate.postForObject(CALC_SERVICE_URL + "/solve/", httpEntity, String.class);
            saveResultsFromJsonData(edition.getId(), result);
            editionService.changeEditionStatus(edition, CLOSED);
        } catch (Exception e) {
            editionService.changeEditionStatus(edition, FAILED);
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
}
