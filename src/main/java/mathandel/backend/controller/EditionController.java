package mathandel.backend.controller;

import mathandel.backend.client.model.EditionTO;
import mathandel.backend.client.model.ProductTO;
import mathandel.backend.client.response.ApiResponse;
import mathandel.backend.security.CurrentUser;
import mathandel.backend.security.UserPrincipal;
import mathandel.backend.service.EditionService;
import mathandel.backend.service.ProductService;
import mathandel.backend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/api/editions")
public class EditionController {

    private final EditionService editionService;
    private final UserService userService;
    private final ProductService productService;

    public EditionController(EditionService editionService, UserService userService, ProductService productService) {
        this.editionService = editionService;
        this.userService = userService;
        this.productService = productService;
    }

    @ResponseStatus(OK)
    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public @ResponseBody ApiResponse createEdition(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody EditionTO editionTO) {
        return editionService.createEdition(editionTO, currentUser.getId());
    }

    @ResponseStatus(OK)
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody List<EditionTO> getEditions() {
        return editionService.getEditions();
    }

    @ResponseStatus(OK)
    @PutMapping("{editionId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public @ResponseBody ApiResponse editEdition(@CurrentUser UserPrincipal currentUser,
                                         @Valid @RequestBody EditionTO editionTO,
                                         @PathVariable("editionId") Long editionId) {
        return editionService.editEdition(editionTO, editionId, currentUser.getId());
    }

    @ResponseStatus(OK)
    @PostMapping("{editionId}/users")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ApiResponse joinEdition(@CurrentUser UserPrincipal currentUser, @PathVariable("editionId") Long editionId) {
        return userService.joinEdition(currentUser.getId(), editionId);
    }

    @ResponseStatus(OK)
    @PutMapping("{editionId}/products/{productId}")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody ApiResponse assignProductToEdition(@CurrentUser UserPrincipal currentUser, @PathVariable("editionId") Long editionId, @PathVariable Long productId) {
        return productService.assignProductToEdition(currentUser.getId(), editionId, productId);
    }

    @ResponseStatus(OK)
    @GetMapping("{editionId}/products")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody Set<ProductTO> getProductsFromEdition(@CurrentUser UserPrincipal currentUser, @PathVariable Long editionId) {
        return productService.getProductsFromEdition(currentUser.getId(), editionId);
    }

    @ResponseStatus(OK)
    @GetMapping("{editionId}/products/my")
    @PreAuthorize("hasRole('USER')")
    public @ResponseBody Set<ProductTO> getMyProductsFromEdition(@CurrentUser UserPrincipal currentUser, @PathVariable Long editionId) {
        return productService.getMyProductsFromEdition(currentUser.getId(), editionId);
    }

    //todo makeUserModeratorOfEdition

}
