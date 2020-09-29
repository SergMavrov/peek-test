package de.peek.maverick.services;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import de.peek.maverick.protocol.AnalyticResponse;
import de.peek.maverick.protocol.SpeechInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PoliticService {

    private static final String SECURITY_TOKEN = "Innere Sicherheit";

    /*
Redner, Thema, Datum, Wörter
Alexander Abel, Bildungspolitik, 2012-10-30, 5310
Bernhard Belling, Kohlesubventionen, 2012-11-05, 1210
Caesare Collins, Kohlesubventionen, 2012-11-06, 1119
Alexander Abel, Innere Sicherheit, 2012-12-11, 911

1. Welcher Politiker hielt im Jahr 2013 die meisten Reden?
2. Welcher Politiker hielt die meisten Reden zum Thema ”Innere Sicherheit”?
3. Welcher Politiker sprach insgesamt die wenigsten Wörter?
     */
    public AnalyticResponse analyzeFiles(List<SpeechInfo> data) {
        // --- analyze security issue
        final Map<String, Integer> mapSecurity = new HashMap<>();
        data.stream().filter(d -> d.getTheme().equals(SECURITY_TOKEN))
                .forEach(d -> updateMapByValue(mapSecurity, d.getSpeaker(), d.getWords()));
        // --- analyze minimum speaking
        final Map<String, Integer> mapMinimum = new HashMap<>();
        data.forEach(d -> updateMapByValue(mapMinimum, d.getSpeaker(), d.getWords()));

        // --- analyze 2013
        final Map<String, Integer> map2013 = new HashMap<>();
        final Calendar calendar = Calendar.getInstance();
        data.stream().filter(d -> {calendar.setTime(d.getDate());
            return calendar.get(Calendar.YEAR) == 2013;
        }).forEach( d -> updateMapByValue(map2013, d.getSpeaker(), d.getWords()));

        // --- final result of analyze
        return AnalyticResponse.builder()
                .leastWordy(getKeyOfValueMinOrMax(mapMinimum, true))
                .mostSecurity(getKeyOfValueMinOrMax(mapSecurity))
                .mostSpeeches(getKeyOfValueMinOrMax(map2013))
                .build();
    }

    private void updateMapByValue(Map<String, Integer> map, String key, Integer value) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + value);
        } else {
            map.put(key, value);
        }
    }

    private String getKeyOfValueMinOrMax(Map<String, Integer> map) {
        return getKeyOfValueMinOrMax(map, false);
    }

    private String getKeyOfValueMinOrMax(Map<String, Integer> map, boolean min) {
        Optional<Map.Entry<String, Integer>> maxEntry;
        if (min) {
            maxEntry = map.entrySet().stream().min(Map.Entry.comparingByValue());
        } else {
            maxEntry = map.entrySet().stream().max(Map.Entry.comparingByValue());
        }
        return maxEntry.map(Map.Entry::getKey).orElse(null);
    }

    public List<SpeechInfo> downloadAndPrepareData(String url) {
        try {
            log.info(String.format("Checking URL %s", url));
            List<SpeechInfo> result = new ArrayList<>();
            URL fileUrl = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
            CSVReader reader = new CSVReader(in);
            String[] line; int i = 0;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            while ((line = reader.readNext()) != null) {
                if (i>0) { // skip the header
                    SpeechInfo current = SpeechInfo.builder()
                            .speaker(line[0].trim())
                            .theme(line[1].trim())
                            .date(format.parse(line[2].trim()))
                            .words(Integer.parseInt(line[3].trim()))
                            .build();
                    log.info(String.format("Added to analyze %s", current));
                    result.add(current);
                }
                i++;
            }
            return result;
        } catch (IOException | CsvValidationException | ParseException e) {
            log.error(String.format("Cannot read provided url %s", url));
            throw new IllegalArgumentException(e);
        }
    }
}
