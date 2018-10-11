package mathandel.backend.controller;

import mathandel.backend.client.model.UserTO;
import mathandel.backend.client.request.PasswordRequest;
import mathandel.backend.client.response.ApiResponse;
import mathandel.backend.security.CurrentUser;
import mathandel.backend.security.UserPrincipal;
import mathandel.backend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(OK)
    @GetMapping("/{userID}")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody UserTO getUserData(@PathVariable("userID") Long userID) {
        return userService.getUserData(userID);
    }

    @ResponseStatus(OK)
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody UserTO getMyData(@CurrentUser UserPrincipal currentUser) {
        UserTO bleble = userService.getUserData(currentUser.getId());
        return bleble;
    }

    @ResponseStatus(OK)
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ApiResponse editMyData(@CurrentUser UserPrincipal userPrincipal, @RequestBody UserTO userTO){
        return userService.editMyData(userPrincipal.getId(), userTO);
    }

    @ResponseStatus(OK)
    @PutMapping("/me/password")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ApiResponse changePassword(@CurrentUser UserPrincipal userPrincipal, @RequestBody PasswordRequest passwordRequest){
        return userService.changePassword(userPrincipal.getId(), passwordRequest.getNewPassword());
    }
}
