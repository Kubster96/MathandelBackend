package mathandel.backend.utils;

//todo should paths be upper case?
public class UrlPaths {

    // /api/auth
    public static final String authPath = "/api/auth";
    // /api/auth/signIn
    public static final String signInPath = authPath + "/signIn";
    // /api/auth/signUp
    public static final String signUpPath = authPath + "/signUp";
    // /api/auth/facebookSignUp
    public static final String facebookSignUp = authPath + "/facebookSignUp";
    // /api/auth/facebookSignIn
    public static final String facebookSignIn = authPath + "/facebookSignIn";

    // /api/editions
    public static final String editionsPath = "/api/editions";
    // /api/editions/{editionId}
    public static final String editionPath = editionsPath + "/{editionId}";
    // /api/editions/{editionId}/participants
    public static final String editionParticipantsPath = editionPath + "/participants";
    // /api/editions/{editionId}/items
    public static final String editionItemsPath = editionPath + "/items";
    // /api/editions/{editionId}/items/{itemId}
    public static final String editionItemPath = editionItemsPath + "/{itemId}";
    // /api/editions/{editionId}/items/my
    public static final String editionMyItemsPath = editionItemsPath + "/my";
    // /api/editions/{editionId}/moderators
    public static final String editionModeratorsPath = editionPath + "/moderators";
    // /api/editions/{editionId}/items/{itemId}/preferences
    public static final String editionItemPreferencePath = editionItemPath + "/preferences";
    // /api/editions/{editionId}/preferences
    public static final String editionPreferencesPath = editionPath + "/preferences";
    // /api/editions/{editionId}/resolve
    public static final String resolveEditionPath = editionPath + "/resolve";
    // /api/editions/{editionId}/publish
    public static final String publishEditionPath = editionPath + "/publish";
    // /api/editions/{editionId}/re-open
    public static final String reOpenEditionPath = editionPath + "/re-open";
    // /api/editions/{editionId}/cancel
    public static final String cancelEditionPath = editionPath + "/cancel";

    // /api/editions/{editionId}/defined-groups
    public static final String definedGroups = editionPath + "/defined-groups";
    // /api/editions/{editionId}/defined-groups/{groupId}
    public static final String definedGroup = definedGroups + "/{groupId}";
    // /api/editions/{editionId}/defined-groups/{groupId}/content
    public static final String definedGroupContent = definedGroup + "/content";

    // /api/items
    public static final String itemsPath = "/api/items";
    // /api/items/{itemId}
    public static final String itemPath = itemsPath + "/{itemId}";
    // /api/items/{itemId}/images
    public static final String itemImagesPath = itemPath + "/images";
    // /api/items/{itemId}/images/{imageName}
    public static final String itemImagePath = itemImagesPath + "/{imageName}";
    // /api/items/not-assigned
    public static final String notAssignedItemsPath = itemsPath + "/not-assigned";
    // /api/items/{itemId}/preferences
    public static final String preferenceForItemPath = itemsPath + "/{itemId}/preference";

    // /api/users
    public static final String usersPath = "/api/users";
    // /api/users/{userId}
    public static final String userPath = usersPath + "/{userId}";
    // /api/users/me
    public static final String userMePath = usersPath + "/me";
    // /api/users/me/password
    public static final String userMePasswordPath = userMePath + "/password";

    // /api/images/{imageName}
    public static final String imagePath = "/images/{imageName}";

    // /api/moderatorRequests
    public static final String moderatorRequestsPath = "/api/moderator-requests";
    // /api/moderator-requests/resolve
    public static final String moderatorRequestsResolvePath = moderatorRequestsPath + "/resolve";
    // /api/moderator-requests/my
    public static final String moderatorRequestsGetMyRequests = moderatorRequestsPath + "/my";

    // /api/results/{resultId}/rate
    public static final String ratePath = "/api/results/{resultId}/rate";
    // /api/users/{userId}/rates
    public static final String userRatesPath = userPath + "/rates";
    // /api/rate-types
    public static final String rateTypesPath = "/api/rate-types";

    // /api/editions/{editionId}/results-send
    public static final String resultsItemsToSendByUserPath = editionPath + "/results-send";
    // /api/editions/{editionId}/results-receive
    public static final String resultsItemsToReceiveByUserPath = editionPath + "/results-receive";

    // /api/editions/{editionId}/results-user
    public static final String editionUserResultsPath = editionPath + "/results-user";
    // /api/editions/{editionId}/results-moderator
    public static final String editionModeratorResultsPath = editionPath + "/results-moderator";
}
