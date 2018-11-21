package mathandel.backend.utils;

import mathandel.backend.model.client.*;
import mathandel.backend.model.server.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerToClientDataConverter {
    public static ModeratorRequestTO mapModeratorRequest(ModeratorRequest moderatorRequest) {
        return new ModeratorRequestTO()
                .setModeratorRequestStatus(moderatorRequest.getModeratorRequestStatus().getName())
                .setReason(moderatorRequest.getReason())
                .setUserId(moderatorRequest.getUser().getId())
                .setId(moderatorRequest.getId());
    }

    public static Set<ModeratorRequestTO> mapModeratorRequests(Set<ModeratorRequest> moderatorRequests){
        return moderatorRequests.stream().map(ServerToClientDataConverter::mapModeratorRequest).collect(Collectors.toSet());
    }

    public static PreferenceTO mapPreference(Preference preference) {
        return new PreferenceTO()
                .setId(preference.getId())
                .setUserId(preference.getUser().getId())
                .setHaveItemId(preference.getHaveItem().getId())
                .setWantedItemsIds(preference.getWantedItems().stream().map(Item::getId).collect(Collectors.toSet()))
                .setWantedDefinedGroupsIds(preference.getWantedDefinedGroups().stream().map(DefinedGroup::getId).collect(Collectors.toSet()));
    }

    public static Set<PreferenceTO> mapPreferences(Set<Preference> preferences) {
        return preferences.stream().map(ServerToClientDataConverter::mapPreference).collect(Collectors.toSet());
    }

    public static String mapRole(Role role){
        return role.getName().toString();
    }

    public static Set<String> mapRoles(Set<Role> roles) {
        return roles.stream().map(ServerToClientDataConverter::mapRole).collect(Collectors.toSet());
    }

    public static ItemTO mapItem(Item item) {
        return new ItemTO()
                .setId(item.getId())
                .setName(item.getName())
                .setDescription(item.getDescription())
                .setUserId(item.getUser().getId())
                .setEditionId(item.getEdition() != null ? item.getEdition().getId() : null)
                .setImages(mapImages(item.getImages()));
    }

    public static Set<ItemTO> mapItems(Set<Item> items) {
        return items.stream().map(ServerToClientDataConverter::mapItem).collect(Collectors.toSet());
    }

    public static DefinedGroupTO mapDefinedGroup(DefinedGroup definedGroup) {
        return new DefinedGroupTO()
                .setId(definedGroup.getId())
                .setName(definedGroup.getName())
                .setItemsIds(definedGroup.getItems().stream().map(Item::getId).collect(Collectors.toSet()))
                .setGroupIds(definedGroup.getGroups().stream().map(DefinedGroup::getId).collect(Collectors.toSet()));
    }

    public static Set<DefinedGroupTO> mapDefinedGroups(Set<DefinedGroup> groups) {
        return groups.stream().map(ServerToClientDataConverter::mapDefinedGroup).collect(Collectors.toSet());
    }

    public static List<EditionTO> mapEditions(List<Edition> all, Long userId) {
        return all.stream().map(edition -> mapEdition(edition, userId)).collect(Collectors.toList());
    }

    public static EditionTO mapEdition(Edition edition, Long userId) {
        return new EditionTO()
                .setId(edition.getId())
                .setName(edition.getName())
                .setDescription(edition.getDescription())
                .setEndDate(edition.getEndDate())
                .setNumberOfParticipants(edition.getParticipants().size())
                .setMaxParticipants(edition.getMaxParticipants())
                .setModerator(edition.getModerators().stream().anyMatch(participant -> participant.getId().equals(userId)))
                .setParticipant(edition.getParticipants().stream().anyMatch(participant -> participant.getId().equals(userId)))
                .setEditionStatusName(edition.getEditionStatusType().getEditionStatusName());
    }

    private static Set<ImageTO> mapImages(Set<Image> images) {
        return images.stream().map(ServerToClientDataConverter::mapImage).collect(Collectors.toSet());
    }

    public static ImageTO mapImage(Image image) {
        return new ImageTO()
                .setId(image.getId())
                .setName(image.getName());
    }

    public static UserTO mapUser(User user) {
        return new UserTO()
                .setId(user.getId())
                .setName(user.getName())
                .setSurname(user.getSurname())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setRoles(mapRoles(user.getRoles()))
                .setAddress(user.getAddress())
                .setCity(user.getCity())
                .setPostalCode(user.getPostalCode())
                .setCountry(user.getCountry());
    }

    public static Set<RateTO> mapRates(Set<Rate> rates) {
        return rates.stream().map(ServerToClientDataConverter::mapRate).collect(Collectors.toSet());
    }

    public static RateTO mapRate(Rate rate) {
        return rate == null ? null : new RateTO()
                .setId(rate.getId())
                .setComment(rate.getComment())
                .setRateTypeName(rate.getRateType().getName())
                .setResultId(rate.getResult().getId())
                .setRaterId(rate.getRater().getId());
    }

    private static ResultTO mapResult(Result result){
        return new ResultTO()
                .setId(result.getId())
                .setReceiver(mapUser(result.getReceiver()))
                .setSender(mapUser(result.getSender()))
                .setItem(mapItem(result.getItemToSend()))
                .setRate(mapRate(result.getRate()));
    }

    public static Set<ResultTO> mapResults(Set<Result> results){
        return results.stream().map(ServerToClientDataConverter::mapResult).collect(Collectors.toSet());
    }

    public static ResultTO mapResultToSend (Result result) {
        return new ResultTO()
                .setId(result.getId())
                .setReceiver(mapUser(result.getReceiver()))
                .setItem(mapItem(result.getItemToSend()))
                .setRate(mapRate(result.getRate()));
    }

    public static Set<ResultTO> mapResultsToSend(Set<Result> results) {
        return results.stream().map(ServerToClientDataConverter::mapResultToSend).collect(Collectors.toSet());
    }

    public static UserTO mapSender(User user) {
        return new UserTO()
                .setName(user.getName())
                .setSurname(user.getSurname())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername());
    }

    public static ResultTO mapResultToReceive (Result result) {
        return new ResultTO()
                .setId(result.getId())
                .setSender(mapSender(result.getSender()))
                .setItem(mapItem(result.getItemToSend()))
                .setRate(mapRate(result.getRate()));
    }

    public static Set<ResultTO> mapResultsToReceive(Set<Result> results) {
        return results.stream().map(ServerToClientDataConverter::mapResultToReceive).collect(Collectors.toSet());
    }

    public static RateTypeTO mapRateType(RateType rateType) {
        return new RateTypeTO()
                .setId(rateType.getId())
                .setRateTypeName(rateType.getName());
    }

    public static Set<RateTypeTO> mapRateTypes(Set<RateType> rateTypes) {
        return rateTypes.stream().map(ServerToClientDataConverter::mapRateType).collect(Collectors.toSet());
    }
}
