package com.bdi.agent.domain;

import com.bdi.agent.model.Action;
import com.bdi.agent.model.Desire;
import com.bdi.agent.repository.ActionRepository;
import com.bdi.agent.service.ActionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockActionRepository"})
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ActionServiceTest {

    @Autowired
    private ActionRepository mockActionRepository;

    @Autowired
    private ActionService actionService;

    @Test
    public void testSetActionsUncompleted() {
        // Setup
        Action action1 = new Action(null, "-", "A1","-", "-", true);
        Action action2 = new Action(null, "-", "A2","-", "-", false);
        Action action3 = new Action(null, "-", "A3","-", "-", true);
        Action action4 = new Action(null, "-", "A4","-", "-", true);
        Desire desire1 = new Desire(1L, null, "", "", true, Set.of(action1));
        Desire desire2 = new Desire(2L, null, "", "", false, Set.of(action2, action3));
        Desire desire3 = new Desire(3L, null, "", "", true, Set.of(action2, action4));
        action1.setDesire(desire1);
        action2.setDesire(desire2);
        action3.setDesire(desire2);
        action4.setDesire(desire3);

        when(mockActionRepository.findByDesireId(1L)).thenReturn(List.of(action1));
        when(mockActionRepository.findByDesireId(2L)).thenReturn(List.of(action2, action3));
        when(mockActionRepository.findByDesireId(3L)).thenReturn(List.of(action4));

        actionService.setActionsUncompleted(List.of(desire1, desire2, desire3));

        Set<Action> actions = Set.of(action1, action2, action3, action4);


        for (Action action : actions) {
            assertThat(action.getCompleted()).isFalse();
            verify(mockActionRepository).save(action);
        }
    }
}
