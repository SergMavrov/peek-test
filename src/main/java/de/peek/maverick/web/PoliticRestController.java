package de.peek.maverick.web;

import de.peek.maverick.protocol.AnalyticResponse;
import de.peek.maverick.protocol.SpeechInfo;
import de.peek.maverick.services.PoliticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class PoliticRestController {

    private final PoliticService politicService;

    @Autowired
    public PoliticRestController(PoliticService politicService) {
        this.politicService = politicService;
    }

    @GetMapping(path = "/evaluation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalyticResponse> analyzeFiles(@RequestParam(name = "url") List<String> urls){
        log.info(String.format("List of URLs received %s", urls));
        final String[] schemes = {"http","https"};
        final UrlValidator urlValidator = new UrlValidator(schemes);
        if (urls.stream().map(urlValidator::isValid).anyMatch(u -> u.equals(false))) {
            return ResponseEntity.badRequest().build();
        };
        List<SpeechInfo> preparedData = new ArrayList<>();
        urls.forEach(url -> preparedData.addAll(politicService.downloadAndPrepareData(url)));
        return ResponseEntity.ok(politicService.analyzeFiles(preparedData));
    }

}
