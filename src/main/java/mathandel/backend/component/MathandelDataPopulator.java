package mathandel.backend.component;

import mathandel.backend.model.server.*;
import mathandel.backend.model.server.enums.EditionStatusName;
import mathandel.backend.model.server.enums.RoleName;
import mathandel.backend.repository.*;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MathandelDataPopulator {

    private final PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private DefinedGroupRepository definedGroupRepository;
    private PreferenceRepository preferenceRepository;
    private RoleRepository roleRepository;
    private EditionRepository editionRepository;
    private EditionStatusTypeRepository editionStatusTypeRepository;
    private ImageRepository imageRepository;

    private Set<String> userNames = new HashSet<>();
    private Set<ItemData> items = new HashSet<>();
    private Set<DefinedGroupData> definedGroups = new HashSet<>();
    private Set<PreferenceLocal> preferences = new HashSet<>();

    private long maxId = 0;
    private Edition edition = null;

    private long lStartTime;
    private long lEndTime;
    private long output;

    @Value("${images.already.downloaded}")
    private boolean imagesDownloaded;

    public MathandelDataPopulator(PasswordEncoder passwordEncoder, UserRepository userRepository, ItemRepository itemRepository, DefinedGroupRepository definedGroupRepository, PreferenceRepository preferenceRepository, RoleRepository roleRepository, EditionRepository editionRepository, EditionStatusTypeRepository editionStatusTypeRepository, ImageRepository imageRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.definedGroupRepository = definedGroupRepository;
        this.preferenceRepository = preferenceRepository;
        this.roleRepository = roleRepository;
        this.editionRepository = editionRepository;
        this.editionStatusTypeRepository = editionStatusTypeRepository;
        this.imageRepository = imageRepository;
    }

    public void saveItemsFromFile(String fileName) throws IOException {

        lStartTime = System.nanoTime();
        initEdition();
        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Created edition -- " + output / 1000000000);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String line = bufferedReader.readLine();
        Pattern p = Pattern.compile("(\\d*)\\. (.*) \\(od (.*)\\)");

        Set<Item> items = new HashSet<>();
        Set<User> users = new HashSet<>();
        HashSet<Role> roles = new HashSet<>();
        Map<String, User> usersMap = new HashMap<>();

        int i = 9999;

        roles.add(roleRepository.findByName(RoleName.ROLE_USER).get());

        while (line != null && !line.equals("\u001A")) {
            line = replacePolishChars(line);

            Matcher matcher = p.matcher(line);
            matcher.matches();
            Long id = Long.parseLong(matcher.group(1));
            String itemName = matcher.group(2);

            itemName = itemName.substring(0, Math.min(250, itemName.length()));
            String userName = matcher.group(3);


            if (usersMap.get(userName) == null) {
                User user = new User()
                        .setName("name" + i)
                        .setUsername(userName)
                        .setSurname("surname" + i)
                        .setPostalCode("code" + i)
                        .setCountry("country" + i)
                        .setCity("city" + i)
                        .setRoles(roles)
                        .setEmail("email" + i + "@domain.com")
                        .setPassword(passwordEncoder.encode(userName + userName))
                        .setAddress("addr" + i);

                users.add(user);
                usersMap.put(userName, user);
            }


            Item e = new Item()
                    .setEdition(edition)
                    .setName(itemName)
                    .setUser(usersMap.get(userName))
                    .setDescription("desc")
                    .setId(id);

            items.add(e);

            line = bufferedReader.readLine();
            i--;
        }

        Set<User> participants = edition.getParticipants();
        participants.addAll(users);
        edition.setParticipants(participants);
        userRepository.saveAll(users);
        editionRepository.save(edition);
        itemRepository.saveAll(items);
    }


    public void saveFromFile(String fileName) throws IOException {

        lStartTime = System.nanoTime();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        String line = bufferedReader.readLine();

        while (line != null && !line.equals("\u001A")) {

            if (line.startsWith("#") || line.isEmpty() || line.startsWith("\n")) {
                line = bufferedReader.readLine();
                continue;
            }

            String[] splitLine = line.split(":");
            String[] leftSideOfCollon = splitLine[0].trim().split("\\)");

            Set<String> rightSideOfCollon = new HashSet<>();
            if (splitLine.length > 1) {
                rightSideOfCollon = Arrays.stream(splitLine[1].trim().split(";")).flatMap(e -> Arrays.stream(e.split(" "))).filter(a -> !a.isEmpty()).collect(Collectors.toSet());
            }

            switch (leftSideOfCollon[1].trim().charAt(0)) {
                case '%':
                    parseDefinedGroupDefinition(leftSideOfCollon, rightSideOfCollon);
                    break;

                default:
                    parsePreference(leftSideOfCollon, rightSideOfCollon);
                    break;
            }
            line = bufferedReader.readLine();
        }
        lEndTime = System.nanoTime();
        System.out.println("Parser  -- " + output / 1000000000);

        saveDataToDb();

    }

    private void initEdition() {

        if (edition == null) {
            EditionStatusType editionStatusType = editionStatusTypeRepository.findByEditionStatusName(EditionStatusName.OPENED);
            Set<User> mods = new HashSet<>();
            Set<User> participants = new HashSet<>();
            User admin = userRepository.findById(1L).get();
            mods.add(admin);
            participants.add(admin);
            edition = new Edition()
                    .setName("Mathandel 30")
                    .setDescription("This is mathandel 30")
                    .setMaxParticipants(200)
                    .setModerators(mods)
                    .setParticipants(participants)
                    .setEditionStatusType(editionStatusType)
                    .setEndDate(LocalDate.now().plusMonths(10));

            editionRepository.save(edition);
        }
    }

    private void saveDataToDb() {
        lStartTime = System.nanoTime();
        saveDefinedGroups();
        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Saved groups -- " + output / 1000000000);
        lStartTime = System.nanoTime();
        saveDefinedGroupsInGroups();
        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Saved groups in groups -- " + output / 1000000000);
        lStartTime = System.nanoTime();
        savePreferences();
        lEndTime = System.nanoTime();
        output = lEndTime - lStartTime;
        System.out.println("Saved preferences -- " + output / 1000000000);
    }


    private void saveDefinedGroups() {
        //zapisac grupsy
        Set<DefinedGroup> localDefinedGroups = new HashSet<>();

        for (DefinedGroupData definedGroupData : definedGroups) {
            DefinedGroup definedGroup = new DefinedGroup()
                    .setName(definedGroupData.getDefinedGruopName())
                    .setUser(userRepository.findByUsername(definedGroupData.getUserName()).get())
                    .setEdition(edition)
                    .setItems(definedGroupData.getItems().stream().map(e -> itemRepository.findById(e).get()).collect(Collectors.toSet()));
            localDefinedGroups.add(definedGroup);
        }

        for (DefinedGroup definedGroup : localDefinedGroups) {
            Set<Item> items = definedGroup.getItems();
            definedGroup.setItems(null);
            DefinedGroup savedGroup = definedGroupRepository.save(definedGroup);
            savedGroup.setItems(items);
            definedGroupRepository.save(savedGroup);
        }
    }

    private void saveDefinedGroupsInGroups() {
        for (DefinedGroupData definedGroupData : definedGroups) {

            String definedGruopName = definedGroupData.getDefinedGruopName();
            //todo this is not necessary
            User user = userRepository.findByUsername(definedGroupData.getUserName()).get();
            DefinedGroup definedGroup = definedGroupRepository.findByNameAndUser_Username(definedGruopName, user.getUsername());

            for (String groupName : definedGroupData.getDefinedGroups()) {
                DefinedGroup groupToAdd = definedGroupRepository.findByNameAndUser_Username(groupName, user.getUsername());
                definedGroup.getGroups().add(groupToAdd);
            }
            definedGroupRepository.save(definedGroup);
        }
    }


    private void savePreferences() {

        Set<Preference> localPreferences = new HashSet<>();

        for (PreferenceLocal preferenceLocal : preferences) {
            User user = userRepository.findByUsername(preferenceLocal.userName).get();
            HashSet<DefinedGroup> groups = new HashSet<>();
            preferenceLocal.definedGroups.forEach(definedGroupName -> {
                DefinedGroup definedGroup = definedGroupRepository.findByNameAndUser(definedGroupName, user);
                groups.add(definedGroup);
            });

            Preference preference = new Preference()
                    .setEdition(edition)
                    .setHaveItem(itemRepository.findById(preferenceLocal.haveItemId).get())
                    .setUser(userRepository.findByUsername(preferenceLocal.userName).get())
                    .setWantedDefinedGroups(groups)
                    .setWantedItems(itemRepository.findAllByIdIn(preferenceLocal.wantedItems));
            localPreferences.add(preference);
        }

        preferenceRepository.saveAll(localPreferences);
    }

    private void parsePreference(String[] leftSideOfCollon, Set<String> rightSideOfCollon) {
        String userName = leftSideOfCollon[0].replace("(", "").replace(")", "");
        userNames.add(userName);

        Long haveItemId = Long.valueOf(leftSideOfCollon[1].trim());
        items.add(new ItemData(userName, haveItemId));
        Set<String> wantedDefinedGroups = new HashSet<>();
        Set<Long> wantedItems = new HashSet<>();

        for (String wantedObject : rightSideOfCollon) {
            switch (wantedObject.trim().charAt(0)) {
                case '%':
                    wantedDefinedGroups.add(getDefinedGroupName(wantedObject));
                    break;
                default:
                    long id = Long.parseLong(wantedObject);
                    if (id > maxId) {
                        maxId = id;
                    }
                    wantedItems.add(id);
                    break;
            }
        }
        preferences.add(new PreferenceLocal(new Preference(), wantedDefinedGroups, userName, haveItemId, wantedItems));
    }

    private void parseDefinedGroupDefinition(String[] leftSideOfCollon, Set<String> rightSideOfCollon) {
        String userName = leftSideOfCollon[0].replace("(", "").replace(")", "");
        userNames.add(userName);
        String definedGroupName = getDefinedGroupName(leftSideOfCollon[1]);

        Set<String> defindedGroupsDefinedGroups = rightSideOfCollon.stream().filter(e -> e.startsWith("%")).map(this::getDefinedGroupName).collect(Collectors.toSet());

//        definedGroups.addAll(defindedGroupsDefinedGroups.stream().map(e -> new DefinedGroupData(null, e, null,null)).collect(Collectors.toSet()));

        Set<Long> definedGroupsItems = rightSideOfCollon.stream().filter(e -> !e.startsWith("%")).map(e -> Long.valueOf(e.trim())).collect(Collectors.toSet());

//        items.addAll(definedGroupsItems.stream().map(id -> new ItemData(null, id)).collect(Collectors.toSet()));

        definedGroups.add(new DefinedGroupData(userName, definedGroupName.trim(), definedGroupsItems, defindedGroupsDefinedGroups));

    }

    private String getDefinedGroupName(String s) {
        return s.replace("%", "");
    }

    public void saveImagesFromUrls(String populateMathandelImageUrlsWithIds) {
        try {
            String content = FileUtils.readFileToString(new File(populateMathandelImageUrlsWithIds));
            JSONObject jsonObject = new JSONObject(content);
            jsonObject.keySet().forEach(itemId -> {
                try {
                    String fileName = itemId + ".jpg";
                    if (!imagesDownloaded) {
                        FileUtils.copyURLToFile(new URL(jsonObject.getString(itemId)), new File("src/main/resources/images/" + fileName));
                    }
                    Item item = itemRepository.findById(Long.parseLong(itemId)).get();
                    Image image = imageRepository.save(new Image().setName(fileName));
                    item.getImages().add(image);
                    itemRepository.save(item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ItemData {

        String userName;
        Long haveItemId;

        ItemData(String userName, Long haveItemId) {

            this.userName = userName;
            this.haveItemId = haveItemId;
        }

    }

    private class PreferenceLocal {

        Preference preference;
        Set<String> definedGroups;
        String userName;
        Long haveItemId;
        Set<Long> wantedItems;

        public PreferenceLocal(Preference preference, Set<String> definedGroups, String userName, Long haveItemId, Set<Long> wantedItems) {
            this.preference = preference;
            this.definedGroups = definedGroups;
            this.userName = userName;
            this.haveItemId = haveItemId;
            this.wantedItems = wantedItems;
        }


    }

    private String replacePolishChars(String line) {
        return line
                .replace("ą", "a")
                .replace("ć", "c")
                .replace("ę", "e")
                .replace("ł", "l")
                .replace("ó", "o")
                .replace("ś", "s")
                .replace("ż", "z")
                .replace("ź", "z")
                .replace("Ą", "A")
                .replace("Ć", "C")
                .replace("Ę", "E")
                .replace("Ł", "L")
                .replace("Ó", "O")
                .replace("Ś", "S")
                .replace("Ż", "Z")
                .replace("Ź", "Z");
    }
}
