package mathandel.backend.controller;

import mathandel.backend.model.client.request.SignInFacebookRequest;
import mathandel.backend.model.client.request.SignInRequest;
import mathandel.backend.model.client.request.SignUpFacebookRequest;
import mathandel.backend.model.client.request.SignUpRequest;
import mathandel.backend.model.client.response.FacebookResponse;
import mathandel.backend.model.client.response.JwtAuthenticationResponse;
import mathandel.backend.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

import static mathandel.backend.utils.UrlPaths.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // documented
    @PostMapping(signInPath)
    public @ResponseBody
    JwtAuthenticationResponse signIn(@Valid @RequestBody SignInRequest signInRequest) {
        return authService.signIn(signInRequest);
    }

    // documented
    @PostMapping(signUpPath)
    public @ResponseBody
    JwtAuthenticationResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    // documented
    @PostMapping(facebookSignIn)
    public @ResponseBody
    FacebookResponse facebookSignIn(@Valid @RequestBody SignInFacebookRequest signInFacebookRequest) {
        return authService.facebookSignIn(signInFacebookRequest);
    }

    // documented
    @PostMapping(facebookSignUp)
    public @ResponseBody
    FacebookResponse facebookSignUp(@Valid @RequestBody SignUpFacebookRequest signUpFacebookRequest) {
        return authService.facebookSignUp(signUpFacebookRequest);
    }
}
