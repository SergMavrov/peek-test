package de.peek.maverick.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith({SpringExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith({SpringExtension.class})
@SpringBootTest
@ActiveProfiles("test")
public class PoliticResControllerTest {

    private final String TEST_NORMAL_URL = "https://dl.dropboxusercontent.com/s/ggjpk809hg3kkof/test-data.csv?dl=0";
    private final String TEST_CORRUPTED_URL = "dl.dropboxusercontent.com/s/ggjpk809hg3kkof/test-data.csv?dl=0";

    protected static final String SLASH = "/";
    public MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }


    /*
    Response:
    Status: 200
    Body: {
”mostSpeeches”: null,
“mostSecurity”: "Alexander Abel",
“leastWordy”: "Caesare Collins"
    }
    */


    @Test
    @Order(0)
    public void testAnalyzeNormalUrl() throws Exception {
        ResultActions resultDropbox = analyzeFiles(TEST_NORMAL_URL);
        resultDropbox
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mostSpeeches", is(equalTo(null))))
                .andExpect(jsonPath("$.mostSecurity", is(equalTo("Alexander Abel"))))
                .andExpect(jsonPath("$.leastWordy", is(equalTo("Caesare Collins"))));
    }

    @Test
    @Order(1)
    public void testAnalyzeCorruptedUrl() throws Exception {
        ResultActions resultDropbox = analyzeFiles(TEST_CORRUPTED_URL);
        resultDropbox
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    protected ResultActions analyzeFiles(String url) throws Exception {
        return mockMvc.perform(get(SLASH +"evaluation" + SLASH + "?url=" + url)
                .contentType(MediaType.APPLICATION_JSON)
        );
    }
}
