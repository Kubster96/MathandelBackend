package mathandel.backend.service;

import mathandel.backend.exception.AppException;
import mathandel.backend.exception.BadRequestException;
import mathandel.backend.exception.ResourceNotFoundException;
import mathandel.backend.model.client.EditionTO;
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

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static mathandel.backend.model.server.enums.EditionStatusName.*;
import static mathandel.backend.utils.ServerToClientDataConverter.mapEdition;

//todo review and refactor
@SuppressWarnings("Duplicates")
@Service
public class CalcService {

    private PreferenceRepository preferenceRepository;
    private DefinedGroupRepository definedGroupRepository;
    private ResultRepository resultRepository;
    private ItemRepository itemRepository;
    private EditionRepository editionRepository;
    private final EditionService editionService;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${calc.service.url}")
    private String CALC_SERVICE_URL;

    @Value("${calc.service.token}")
    private String CALC_SERVICE_TOKEN;

    public CalcService(PreferenceRepository preferenceRepository, DefinedGroupRepository definedGroupRepository, ResultRepository resultRepository, ItemRepository itemRepository, EditionRepository editionRepository, EditionService editionService, RestTemplate restTemplate, UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.definedGroupRepository = definedGroupRepository;
        this.resultRepository = resultRepository;
        this.itemRepository = itemRepository;
        this.editionRepository = editionRepository;
        this.editionService = editionService;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }


    public EditionTO resolveEdition(Long userId, Long editionId) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));
        EditionStatusName editionStatusName = edition.getEditionStatusType().getEditionStatusName();

        if (!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }
        if (!(editionStatusName.equals(OPENED) || editionStatusName.equals(FAILED) || editionStatusName.equals(CLOSED))) {
            throw new BadRequestException("You cannot resolve edition with edition status " + editionStatusName);
        }

        edition = editionService.changeEditionStatus(edition, PENDING);

        ResultCalculator resultCalculator = new ResultCalculator(resultRepository, CALC_SERVICE_TOKEN, restTemplate, edition, CALC_SERVICE_URL, editionService, preferenceRepository, definedGroupRepository, itemRepository, editionRepository);
        Thread resultCalculatorThread = new Thread(resultCalculator);
        resultCalculatorThread.start();

        return mapEdition(edition, userId);
    }

    @Transactional
    public Edition calculateResults(Edition edition) {

        resultRepository.deleteAllByEdition_Id(edition.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", CALC_SERVICE_TOKEN);
        String body = getMappedDataForEdition(edition.getId());
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        try {
            String result = restTemplate.postForObject(CALC_SERVICE_URL + "/solve/", httpEntity, String.class);
            saveResultsFromJsonData(edition.getId(), result);
            return editionService.changeEditionStatus(edition, CLOSED);
        } catch (Exception e) {
            return editionService.changeEditionStatus(edition, FAILED);
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

    private void nullEditionIdsOfAllItemsThatWereNotChosen(Long editionId) {
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

    public EditionTO publishEdition(Long userId, Long editionId) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));

        if (!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }
        if (!edition.getEditionStatusType().getEditionStatusName().equals(CLOSED)) {
            throw new BadRequestException("You have to resolve edition first");
        }

        nullEditionIdsOfAllItemsThatWereNotChosen(editionId);
        return mapEdition(editionService.changeEditionStatus(edition, PUBLISHED), userId);
    }

    @Transactional
    public EditionTO reOpenEdition(Long userId, Long editionId, LocalDate endDate) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));
        EditionStatusName editionStatusName = edition.getEditionStatusType().getEditionStatusName();

        if (!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }
        if (!(editionStatusName.equals(CLOSED) || editionStatusName.equals(FAILED))) {
            throw new BadRequestException("You can only reopen closed or failed edition edition is " + editionStatusName);
        }
        if (endDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Edition end date cannot be in the past");
        }
        System.out.println(LocalDate.now());
        System.out.println(endDate);

        resultRepository.deleteAllByEdition_Id(edition.getId());
        return mapEdition(editionService.changeEditionStatus(edition, OPENED).setEndDate(endDate), userId);

    }

    private void nullEditionIdsOfAllItems(Long editionId) {
        Set<Item> notAssignedItems = new HashSet<>();

        itemRepository.findAllByEdition_Id(editionId).forEach(item -> {
            item.setEdition(null);
            notAssignedItems.add(item);
        });

        itemRepository.saveAll(notAssignedItems);
    }

    @Transactional
    public EditionTO cancelEdition(Long userId, Long editionId) {
        User moderator = userRepository.findById(userId).orElseThrow(() -> new AppException("User does not exist"));
        Edition edition = editionRepository.findById(editionId).orElseThrow(() -> new ResourceNotFoundException("Edition", "id", editionId));
        EditionStatusName editionStatusName = edition.getEditionStatusType().getEditionStatusName();

        if (!edition.getModerators().contains(moderator)) {
            throw new BadRequestException("You have no access to this resource");
        }
        if (!(editionStatusName.equals(OPENED) || editionStatusName.equals(FAILED) || editionStatusName.equals(CLOSED))) {
            throw new BadRequestException("You cannot cancel " + editionStatusName + " edition");
        }

        resultRepository.deleteAllByEdition_Id(edition.getId());
        definedGroupRepository.deleteAllByEdition_Id(edition.getId());
        preferenceRepository.deleteAllByEdition_Id(edition.getId());
        edition.getParticipants().clear();
        editionRepository.save(edition);

        nullEditionIdsOfAllItems(edition.getId());
        return mapEdition(editionService.changeEditionStatus(edition, CANCELLED), userId);
    }
}
