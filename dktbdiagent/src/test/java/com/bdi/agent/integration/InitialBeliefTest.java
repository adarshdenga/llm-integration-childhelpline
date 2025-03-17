package com.bdi.agent.integration;

import com.bdi.agent.model.Belief;
import com.bdi.agent.service.BeliefService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"mockBeliefRepository", "mockAgentRepository", "mockSimpMessagingTemplate"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
@AutoConfigureMockMvc
public class InitialBeliefTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private transient BeliefService beliefService;

    @Test
    void testGetAllBeliefsFromCsvValid() throws Exception {
        List<Belief> beliefs = List.of(
                new Belief("B1", "Test Belief 1", 0.5f),
                new Belief("B2", "Test Belief 2", 0.7f),
                new Belief("B3", "Test Belief 3", 0.3f)
        );

        Mockito.when(beliefService.getAllInitialBeliefsSorted()).thenReturn(beliefs);

        mockMvc.perform(get("/beliefs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("B1"))
                .andExpect(jsonPath("$[0].value").value(0.5))
                .andExpect(jsonPath("$[0].fullName").value("Test Belief 1"))
                .andExpect(jsonPath("$[1].id").value("B2"))
                .andExpect(jsonPath("$[1].value").value(0.7))
                .andExpect(jsonPath("$[1].fullName").value("Test Belief 2"))
                .andExpect(jsonPath("$[2].id").value("B3"))
                .andExpect(jsonPath("$[2].value").value(0.3))
                .andExpect(jsonPath("$[2].fullName").value("Test Belief 3"));
    }
}
