package mathandel.backend.controller;

import mathandel.backend.client.response.ApiResponse;
import mathandel.backend.model.server.enums.EditionStatusName;
import mathandel.backend.security.CurrentUser;
import mathandel.backend.security.UserPrincipal;
import mathandel.backend.service.CalcService;
import mathandel.backend.service.EditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static mathandel.backend.utils.UrlPaths.closeEditionPath;

@Controller
public class CalcControler {

    @Autowired
    private RestTemplate restTemplate;

    private CalcService calcService;

    private EditionService editionService;

    private String CALC_SERVIE_URL = "http://localhost:8080";

//    private static String WORKDIR;
//
//    private static String REQUEST_TO_DESERIALIZE_PATH;

    public CalcControler(CalcService calcService, EditionService editionService) {
        this.calcService = calcService;
        this.editionService = editionService;
    }


    @PostMapping(closeEditionPath)
    @PreAuthorize("hasRole('MODERATOR')")
    public @ResponseBody
    ApiResponse closeEdition(@CurrentUser UserPrincipal userPrincipal, @PathVariable Long editionId) throws IOException {

        // todo check Premissions after merge and if edition exists

        String url = CALC_SERVIE_URL + "/api/solve"; //  todo wklepac jakis uniwersalny

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        editionService.changeEditionStatus(userPrincipal.getId(), editionId, EditionStatusName.CLOSED);
        String body = calcService.getMappedDataForEdition(editionId);
        // for testing
//        WORKDIR = System.getProperty("user.dir");
//        REQUEST_TO_DESERIALIZE_PATH = "/src/main/resources/exampleCalsServiceResponseData.json";
//        String body = new String(Files.readAllBytes(Paths.get(WORKDIR + REQUEST_TO_DESERIALIZE_PATH)), StandardCharsets.UTF_8);

        Map bodyMap = new HashMap<String, String>();
        bodyMap.put("body", body);


        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(bodyMap, headers);

        String result = restTemplate.postForObject(url, httpEntity, String.class);

        calcService.saveResultsFromJsonData(editionId, result);

        editionService.changeEditionStatus(userPrincipal.getId(), editionId, EditionStatusName.FINISHED);

        return new ApiResponse("Edition closed, you can now check for results");

    }


}
