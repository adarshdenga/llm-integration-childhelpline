package com.bdi.agent.domain;

import com.bdi.agent.TestConfig;
import com.bdi.agent.TestUtils;
import com.bdi.agent.model.dto.BeliefDto;
import com.bdi.agent.model.dto.DesireDto;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.service.ConstraintService;
import com.bdi.agent.service.graph.GraphUtilsService;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles({"mockConstraintService"})
@TestPropertySource(locations="classpath:application-test.properties")
public class GraphUtilsServiceTest {
    @Autowired
    private  transient GraphUtilsService graphUtils;

    @Autowired
    private transient ConstraintService mockConstraintService;

    @Autowired
    private transient TestUtils utils;

    /**
     * Mocks response of mockConstraintService.checkDesireConstraints() given
     * the values per desire.
     *
     * @param values The value per desire (true or false)
     */
    private void mockDesireActivity(boolean[] values) {
        int idx = 0;
        for(DesireName name : DesireName.values()) {
            Mockito.doReturn(values[idx]).when(mockConstraintService)
                    .checkDesireConstraints(eq(name), any());
            idx++;
        }
    }

    @Test
    public void testBeliefArrayToDtoListValid() {
        GraphUtilsService spyGraphUtils = Mockito.spy(graphUtils);

        Mockito.doReturn("Full name 1").when(spyGraphUtils).getFullBeliefName(BeliefName.B1);
        Mockito.doReturn("Full name 2").when(spyGraphUtils).getFullBeliefName(BeliefName.B2);
        Mockito.doReturn("Full name 3").when(spyGraphUtils).getFullBeliefName(BeliefName.B3);

        assertThat(spyGraphUtils.beliefArrayToDtoList(new float[]{0.1f, 0.2f, 0.3f}))
                .containsExactly(
                        new BeliefDto("B1", "Full name 1", 0.1f),
                        new BeliefDto("B2", "Full name 2", 0.2f),
                        new BeliefDto("B3", "Full name 3", 0.3f)
                );
    }

    @Test
    public void testBeliefArrayToDtoListInvalid() {
        GraphUtilsService spyGraphUtils = Mockito.spy(graphUtils);

        Mockito.doReturn("Full name").when(spyGraphUtils).getFullBeliefName(any());

        // Check that if there are more values than beliefs, null is returned
        int amountOfBeliefs = BeliefName.values().length;
        assertThat(spyGraphUtils.beliefArrayToDtoList(new float[amountOfBeliefs + 1]))
                .isNull();
    }

    @Test
    public void testGetDesiresList() {
        // Setup and mocking responses
        float[] anyValues = utils.getFilledBeliefArray(utils.getMinValue());
        GraphUtilsService spyGraphUtils = Mockito.spy(graphUtils);
        boolean[] valueArray = new boolean[]{true, false, true, false, true};
        mockDesireActivity(valueArray);
        Mockito.doReturn("Full name").when(spyGraphUtils).getFullDesireName(any());

        assertThat(spyGraphUtils.getDesiresList(anyValues)).containsExactly(
                new DesireDto(DesireName.D1, "Full name", valueArray[0]),
                new DesireDto(DesireName.D2, "Full name", valueArray[1]),
                new DesireDto(DesireName.D3, "Full name", valueArray[2]),
                new DesireDto(DesireName.D4, "Full name", valueArray[3]),
                new DesireDto(DesireName.D5, "Full name", valueArray[4])
        );
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.1, GEQ", "0.2, 0.1, GEQ", "0.1, 0.1, EQ", "0.2, 0.1, EQ", "0.2, 0.1, GT",
            "0.2, 0.1, LT", "0.1, 0.2, LT", "0.1, 0.2, LEQ", "0.2, 0.1, LEQ"})
    public void testNeedToIncreaseFalse(float currentValue, float goalValue, BoundaryCheck boundaryCheck) {
        assertThat(graphUtils.needToIncrease(currentValue,goalValue, boundaryCheck)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.1, GT", "0.1, 0.2, GEQ", "0.1, 0.2, EQ", "0.1, 0.2, GT"})
    public void testNeedToIncreaseTrue(float currentValue, float goalValue, BoundaryCheck boundaryCheck) {
        assertThat(graphUtils.needToIncrease(currentValue,goalValue, boundaryCheck)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0.2, 0.1, GEQ", "0.1, 0.1, LEQ", "0.1, 0.2, LEQ", "0.1, 0.1, EQ", "0.1, 0.2, LT",
            "0.2, 0.1, GT"})
    public void testStopUpdatingTrue(float currentValue, float goalValue, BoundaryCheck boundaryCheck) {
        assertThat(graphUtils.stopUpdating(currentValue,goalValue, boundaryCheck)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0.1, 0.2, GEQ", "0.1, 0.2, GT", "0.1, 0.1, GT", "0.2, 0.1, LEQ", "0.2, 0.1, LT",
            "0.1, 0.1, LT", "0.1, 0.2, EQ", "0.2, 0.1, EQ"})
    public void testStopUpdatingFalse(float currentValue, float goalValue, BoundaryCheck boundaryCheck) {
        assertThat(graphUtils.stopUpdating(currentValue,goalValue, boundaryCheck)).isFalse();
    }

    @Test
    public void testCalculateRelatedness() {
        // The relatedness beliefs are also defined in this file
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());

        for (BeliefName name : utils.getRelatednessBeliefs()) {
            beliefs[utils.getBeliefOrdering().indexOf(name)] = utils.getMaxValue();
        }

        assertThat(graphUtils.calculateRelatedness(beliefs)).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));

        for (BeliefName name : utils.getRelatednessBeliefs()) {
            beliefs[utils.getBeliefOrdering().indexOf(name)] = utils.getMinValue();
        }

        assertThat(graphUtils.calculateRelatedness(beliefs)).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));
    }

    @Test
    public void testGetBeliefValue() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());
        beliefs[0] = utils.getMaxValue();

        // BeliefOrdering is also an attribute in the tests here
        assertThat(graphUtils.getBeliefValue(utils.getBeliefOrdering().get(0), beliefs))
                .isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));
        assertThat(graphUtils.getBeliefValue(utils.getBeliefOrdering().get(1), beliefs))
                .isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));
    }

    @Test
    public void testSetBeliefValue() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());

        assertThat(beliefs[0]).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));
        graphUtils.setBeliefValue(utils.getBeliefOrdering().get(0), beliefs, utils.getMaxValue());
        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));
    }

    @Test
    public void testIncreaseBeliefValueValid() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());

        assertThat(beliefs[0]).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));
        graphUtils.increaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, 0.1f);
        assertThat(beliefs[0]).isCloseTo(utils.getMinValue() + 0.1f, Offset.offset(0.0001f));
    }

    @Test
    public void testIncreaseBeliefValueInvalid() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMaxValue());

        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));

        // Try increasing above utils.getMaxValue()
        graphUtils.increaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, 0.1f);
        // Assert it did not change
        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));

        beliefs[0] = utils.getMinValue();
        // Try increasing by negative amount
        graphUtils.increaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, -0.1f);
        // Assert it did not change
        assertThat(beliefs[0]).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));
    }

    @Test
    public void testDecreaseBeliefValueValid() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMaxValue());

        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));
        graphUtils.decreaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, 0.1f);
        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue() - 0.1f, Offset.offset(0.0001f));
    }

    @Test
    public void testDecreaseBeliefValueInvalid() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());

        assertThat(beliefs[0]).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));

        // Try decreasing below utils.getMinValue()
        graphUtils.decreaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, 0.1f);
        // Assert it did not change
        assertThat(beliefs[0]).isCloseTo(utils.getMinValue(), Offset.offset(0.0001f));

        beliefs[0] = utils.getMaxValue();
        // Try decreasing by negative amount
        graphUtils.decreaseBeliefValue(utils.getBeliefOrdering().get(0), beliefs, -0.1f);
        // Assert it did not change
        assertThat(beliefs[0]).isCloseTo(utils.getMaxValue(), Offset.offset(0.0001f));
    }

    @Test
    public void testGetPhaseByDesire() {
        /*
         * Note for future adjustments: This method and therefore this test checks the
         * current implementation of the phases. If it is changed, these need to be adapted.
         */

        assertThat(graphUtils.getPhaseByDesire(DesireName.D1)).isEqualTo(Phase.PHASE2);
        assertThat(graphUtils.getPhaseByDesire(DesireName.D2)).isEqualTo(Phase.PHASE5);
        assertThat(graphUtils.getPhaseByDesire(DesireName.D3)).isEqualTo(Phase.PHASE3);
        assertThat(graphUtils.getPhaseByDesire(DesireName.D4)).isEqualTo(Phase.PHASE4);
        assertThat(graphUtils.getPhaseByDesire(DesireName.D5)).isEqualTo(Phase.PHASE3);
        assertThat(graphUtils.getPhaseByDesire(null)).isEqualTo(Phase.PHASE1);
    }

    @Test
    public void testGetActiveDesireNonNull() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());
        boolean[] valueArray = new boolean[]{false, true, false, false, false};
        mockDesireActivity(valueArray);

        assertThat(graphUtils.getActiveDesire(beliefs)).isEqualTo(DesireName.values()[1]);
    }

    @Test
    public void testGetActiveDesireNull() {
        float[] beliefs = utils.getFilledBeliefArray(utils.getMinValue());
        boolean[] valueArray = new boolean[]{false, false, false, false, false};
        mockDesireActivity(valueArray);

        // No desire is active, so should return null
        assertThat(graphUtils.getActiveDesire(beliefs)).isNull();

    }

}
