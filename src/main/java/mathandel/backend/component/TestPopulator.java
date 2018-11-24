package mathandel.backend.component;

import mathandel.backend.model.client.EditionTO;
import mathandel.backend.model.client.PreferenceTO;
import mathandel.backend.model.client.request.CreateUpdateItemRequest;
import mathandel.backend.model.client.request.SignUpRequest;
import mathandel.backend.service.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;

/*
Readme

this is just a component for testing

if you want to use it add @Transactional annotations in methods
    - UserService.joinEdition()
    - PreferenceService.updatePreference()
    - EditionService.createEdition()
    - ItemService.createItem()
    - CalcService.resolveEdition()

otherwise lazy fetch error will occur

 */
@Component
public class TestPopulator {

    private final AuthService authService;
    private final EditionService editionService;
    private final ItemService itemService;
    private final PreferenceService preferenceService;
    private final CalcService calcService;
    private final UserService userService;

    public TestPopulator(AuthService authService, EditionService editionService, ItemService itemService, PreferenceService preferenceService, CalcService calcService, UserService userService) {
        this.authService = authService;
        this.editionService = editionService;
        this.itemService = itemService;
        this.preferenceService = preferenceService;
        this.calcService = calcService;
        this.userService = userService;
    }

    public void populate() {
        createEdition();
        createUsers();
        createItems();
        createPreferences();
//        resolveEdition();
    }

    private void createEdition() {
        EditionTO editionTO = new EditionTO()
                .setName("Mathandel")
                .setMaxParticipants(10)
                .setDescription("Description")
                .setEndDate(LocalDate.now().plusMonths(10));

        editionService.createEdition(editionTO, 1L);
    }

    private void createUsers() {
        SignUpRequest signUpRequest1 = new SignUpRequest()
                .setUsername("user1")
                .setEmail("user1user1@user1.com")
                .setPassword("user1user1")
                .setName("user1")
                .setSurname("user1")
                .setAddress("user1")
                .setCity("user1")
                .setCountry("user1")
                .setPostalCode("user1");

        SignUpRequest signUpRequest2 = new SignUpRequest()
                .setUsername("user2")
                .setEmail("user2user2@user2.com")
                .setPassword("user2user2")
                .setName("user2")
                .setSurname("user2")
                .setAddress("user2")
                .setCity("user2")
                .setCountry("user2")
                .setPostalCode("user2");

        authService.signUp(signUpRequest1);
        userService.joinEdition(2L, 1L);
        authService.signUp(signUpRequest2);
        userService.joinEdition(3L, 1L);
    }

    private void createItems() {
        CreateUpdateItemRequest createUpdateItemRequest1 = new CreateUpdateItemRequest()
                .setName("item1")
                .setDescription("item1 description")
                .setImagesToRemove(Collections.emptySet());

        CreateUpdateItemRequest createUpdateItemRequest2 = new CreateUpdateItemRequest()
                .setName("item2")
                .setDescription("item2 description")
                .setImagesToRemove(Collections.emptySet());

        CreateUpdateItemRequest createUpdateItemRequest3 = new CreateUpdateItemRequest()
                .setName("item3")
                .setDescription("item3 description")
                .setImagesToRemove(Collections.emptySet());

        itemService.createItem(1L, 1L, createUpdateItemRequest1, null, null, null);
        itemService.createItem(2L, 1L, createUpdateItemRequest2, null, null, null);
        itemService.createItem(3L, 1L, createUpdateItemRequest3, null, null, null);
    }

    private void createPreferences() {
        PreferenceTO preference1 = new PreferenceTO()
                .setWantedItemsIds(Collections.singleton(2L))
                .setWantedDefinedGroupsIds(Collections.emptySet());
        PreferenceTO preference2 = new PreferenceTO()
                .setWantedItemsIds(Collections.singleton(3L))
                .setWantedDefinedGroupsIds(Collections.emptySet());
        PreferenceTO preference3 = new PreferenceTO()
                .setWantedItemsIds(Collections.singleton(1L))
                .setWantedDefinedGroupsIds(Collections.emptySet());

        preferenceService.updatePreference(1L, preference1, 1L, 1L);
        preferenceService.updatePreference(2L, preference2, 1L, 2L);
        preferenceService.updatePreference(3L, preference3, 1L, 3L);
    }

    private void closeEdition() {
        calcService.resolveEdition(1L, 1L);
    }
}
