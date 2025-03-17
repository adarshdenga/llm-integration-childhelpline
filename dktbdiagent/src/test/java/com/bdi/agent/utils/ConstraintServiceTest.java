package com.bdi.agent.utils;

import com.bdi.agent.TestConfig;
import com.bdi.agent.TestUtils;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.util.BeliefConstraint;
import com.bdi.agent.model.util.PhaseTransitionConstraints;
import com.bdi.agent.service.ConstraintService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles({"mockConstraintProvider"})
@TestPropertySource(locations="classpath:application-test.properties")
public class ConstraintServiceTest {
    private transient final ConstraintProvider mockConstraintProvider;
    private transient final ConstraintService constraintService;
    private transient final TestUtils utils;

    @Autowired
    public ConstraintServiceTest(ConstraintProvider mockConstraintProvider, ConstraintService constraintService,
                                 TestUtils utils) {
        this.mockConstraintProvider = mockConstraintProvider;
        this.constraintService = constraintService;
        this.utils = utils;
    }

    @Test
    public void testIsConstraintSatisfiedInvalid() {
        // Testing idx == -1 is not possible, since the beliefOrdering should always contain all enum values
        // Test idx >= beliefs.length
        BeliefConstraint beliefConstraint = new BeliefConstraint(BoundaryCheck.EQ,
                utils.getBeliefOrdering().get(0), 0);
        assertThat(constraintService.isConstraintSatisfied(beliefConstraint, new float[]{})).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"GEQ, 0, 0, 0", "LEQ, 0, 0, 0", "GT, 0, 0, 0.1", "LT, 0, 0.1, 0", "GEQ, 0, 0, 0.1",
            "LEQ, 0, 0.1, 0", "EQ, 0, 0, 0", "NEQ, 0, 0.1, 0", "NEQ, 0, 0, 0.1"})
    public void testIsConstraintSatisfiedSatisfied(BoundaryCheck boundaryCheck, int beliefIdx, float goalValue,
                                                   float fillValue) {
        float[] beliefs = utils.getFilledBeliefArray(fillValue);

        BeliefConstraint beliefConstraint = new BeliefConstraint(boundaryCheck,
                utils.getBeliefOrdering().get(beliefIdx), goalValue);

        assertThat(constraintService.isConstraintSatisfied(beliefConstraint, beliefs)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"GEQ, 0, 0.1, 0", "LEQ, 0, 0, 0.1", "GT, 0, 0, 0", "LT, 0, 0, 0", "GT, 0, 0.1, 0",
            "LT, 0, 0, 0.1", "EQ, 0, 0.1, 0", "EQ, 0, 0, 0.1", "NEQ, 0, 0, 0"})
    public void testIsConstraintSatisfiedNotSatisfied(BoundaryCheck boundaryCheck, int beliefIdx, float goalValue,
                                                      float fillValue) {
        float[] beliefs = utils.getFilledBeliefArray(fillValue);

        BeliefConstraint beliefConstraint = new BeliefConstraint(boundaryCheck,
                utils.getBeliefOrdering().get(beliefIdx), goalValue);

        assertThat(constraintService.isConstraintSatisfied(beliefConstraint, beliefs)).isFalse();
    }

    @Test
    public void testDnfConstraintsSatisfied_OnlyDisjunction() {
        // Tests DNF of the form: x1 || x2 || x3

        float[] beliefs = utils.getFilledBeliefArray(0);

        // All true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0))
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1))
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // None true
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1))
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();
    }

    @Test
    public void testDnfConstraintsSatisfied_OnlyConjunction() {
        // Tests DNF of the form: (x1 && x2 && x3)

        float[] beliefs = utils.getFilledBeliefArray(0);

        // All true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0)
        ));
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0)
        ));
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet2, beliefs)).isFalse();

        // None true
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)
        ));
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();
    }

    @Test
    public void testDnfConstraintsSatisfied_SingleAtom() {
        // Tests DNF of the form: x1

        float[] beliefs = utils.getFilledBeliefArray(0);

        // True
        Set<Set<BeliefConstraint>> constraintSet = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
        ));
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // False
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
        ));
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet2, beliefs)).isFalse();
    }

    @Test
    public void testDnfConstraintsSatisfied_DisjunctionOfConjunctions() {
        // Tests DNF of the form: (x1 && x2) || (x3 && x4)

        float[] beliefs = utils.getFilledBeliefArray(0);

        // Both conjunctions true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 0)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One conjunction true, other partially false
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // One conjunction true, other fully false
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet3, beliefs)).isTrue();

        // Both conjunctions partially false
        Set<Set<BeliefConstraint>> constraintSet4 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet4, beliefs)).isFalse();

        // Both conjunctions fully false
        Set<Set<BeliefConstraint>> constraintSet5 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet5, beliefs)).isFalse();
    }

    @Test
    public void testDnfConstraintsSatisfied_DisjunctionMixed() {
        // Tests DNF of the form: (x1 && x2) || x3

        float[] beliefs = utils.getFilledBeliefArray(0);

        // Conjunction true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // Atom true
        Set<Set<BeliefConstraint>> constraintSet1 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet1, beliefs)).isTrue();

        // Both true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // Both false
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
                )
        );
        assertThat(constraintService.areDnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();
    }

    @Test
    public void testCnfConstraintsSatisfied_OnlyConjunction() {
        // Tests CNF of the form: (x1) && (x2) && (x3)

        float[] beliefs = utils.getFilledBeliefArray(0);

        // All true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0))
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1))
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet2, beliefs)).isFalse();

        // None true
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1))
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();
    }

    @Test
    public void testCnfConstraintsSatisfied_OnlyDisjunction() {
        // Tests CNF of the form: (x1 || x2 || x3)

        float[] beliefs = utils.getFilledBeliefArray(0);

        // All true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0)
        ));
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0)
        ));
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // None true
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)
        ));
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();
    }

    @Test
    public void testCnfConstraintsSatisfied_SingleAtom() {
        // Tests CNF of the form: x1

        float[] beliefs = utils.getFilledBeliefArray(0);

        // True
        Set<Set<BeliefConstraint>> constraintSet = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
        ));
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // False
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
        ));
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet2, beliefs)).isFalse();
    }

    @Test
    public void testCnfConstraintsSatisfied_ConjunctionOfDisjunctions() {
        // Tests CNF of the form: (x1 || x2) && (x3 || x4)

        float[] beliefs = utils.getFilledBeliefArray(0);

        // Both disjunctions true
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 0)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet, beliefs)).isTrue();

        // One disjunction true, other partially true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // One disjunction true, other fully false
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet3, beliefs)).isFalse();

        // Both disjunctions fully false
        Set<Set<BeliefConstraint>> constraintSet4 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet4, beliefs)).isFalse();
    }

    @Test
    public void testCnfConstraintsSatisfied_ConjunctionMixed() {
        // Tests CNF of the form: (x1 || x2) && x3

        float[] beliefs = utils.getFilledBeliefArray(0);

        // Disjunction true, atom false
        Set<Set<BeliefConstraint>> constraintSet = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet, beliefs)).isFalse();

        // Atom true, disjunction false
        Set<Set<BeliefConstraint>> constraintSet1 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet1, beliefs)).isFalse();


        // Atom true, disjunction partially true
        Set<Set<BeliefConstraint>> constraintSet2 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
                )
        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet2, beliefs)).isTrue();

        // Both true
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 0)
                )

        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet3, beliefs)).isTrue();

        // Both false
        Set<Set<BeliefConstraint>> constraintSet4 = Set.of(
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)
                ),
                Set.of(
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
                )

        );
        assertThat(constraintService.areCnfConstraintsSatisfied(constraintSet4, beliefs)).isFalse();
    }

    @Test
    public void testCheckDesireConstraintsCheckCalls() {
        /*
        This only tests that the correct methods are called. It is closely bound to the code since these are the
        methods that should be used currently, and it only "forwards" some data.
         */

        Mockito.reset(mockConstraintProvider);
        when(mockConstraintProvider.getDesireConstraints(DesireName.D1)).thenReturn(new HashSet<>());

        ConstraintService spyConstraintService = Mockito.spy(constraintService);

        when(spyConstraintService.areDnfConstraintsSatisfied(new HashSet<>(), new float[]{}))
                .thenReturn(true);
        assertThat(spyConstraintService.checkDesireConstraints(DesireName.D1, new float[]{})).isTrue();

        when(spyConstraintService.areDnfConstraintsSatisfied(new HashSet<>(), new float[]{}))
                .thenReturn(false);
        assertThat(spyConstraintService.checkDesireConstraints(DesireName.D1, new float[]{})).isFalse();

        verify(spyConstraintService, times(2)).areDnfConstraintsSatisfied(new HashSet<>(), new float[]{});
        verify(mockConstraintProvider, times(2)).getDesireConstraints(DesireName.D1);
    }

    @Test
    public void testCheckDesireConstraintsCheckExample() {
        // This tests an example input, which is false for DNF and true for CNF

        Mockito.reset(mockConstraintProvider);
        Set<Set<BeliefConstraint>> constraintSet = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 0)
        ));

        when(mockConstraintProvider.getDesireConstraints(DesireName.D1)).thenReturn(constraintSet);

        assertThat(constraintService.checkDesireConstraints(DesireName.D1, new float[]{})).isFalse();
        verify(mockConstraintProvider).getDesireConstraints(DesireName.D1);
    }

    @Test
    public void testEmptyCnfIsTrue() {
        assertThat(constraintService.areCnfConstraintsSatisfied(Set.of(), new float[]{})).isTrue();
        assertThat(constraintService.areCnfConstraintsSatisfied(Set.of(Set.of()), new float[]{})).isTrue();
    }

    @Test
    public void testEmptyDnfIsTrue() {
        assertThat(constraintService.areDnfConstraintsSatisfied(Set.of(), new float[]{})).isTrue();
        assertThat(constraintService.areDnfConstraintsSatisfied(Set.of(Set.of()), new float[]{})).isTrue();
    }

    @Test
    public void testConstraintSetEmpty() {
        assertThat(constraintService.isConstraintSetEmpty(Set.of(Set.of()))).isTrue();
        assertThat(constraintService.isConstraintSetEmpty(Set.of())).isTrue();
        assertThat(constraintService.isConstraintSetEmpty(Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)
        )))).isFalse();
    }

    @Test
    public void testImpossibleToReachPhaseFiveValid() {
        // Mock constraints for phases
        Set<Set<BeliefConstraint>> constraintSet3 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)));
        PhaseTransitionConstraints constraints3 = new PhaseTransitionConstraints(
            Phase.PHASE3, Phase.PHASE4, Map.of(), constraintSet3, Set.of(), new float[]{});
        Set<Set<BeliefConstraint>> constraintSet4 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)));
        PhaseTransitionConstraints constraints4 = new PhaseTransitionConstraints(
                Phase.PHASE4, Phase.PHASE5, Map.of(), constraintSet4, Set.of(), new float[]{});

        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE3)).thenReturn(constraints3);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE4)).thenReturn(constraints4);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE5)).thenReturn(null);

        // Assert on constraints 3 -> 5, 4 -> 5, 5 -> 5
        Set<Set<BeliefConstraint>> allConstraintsFrom3 = Set.of(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                        new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)));
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE3))
                .isEqualTo(allConstraintsFrom3);
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE4)).isEqualTo(constraintSet4);
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE5)).isEqualTo(new HashSet<>());
    }

    @Test
    public void testImpossibleToReachPhaseFiveNull() {
        // Mock constraints for phase
        Set<Set<BeliefConstraint>> constraintSet4 = Set.of(Set.of(
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1),
                new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 0)));
        PhaseTransitionConstraints constraints4 = new PhaseTransitionConstraints(
                Phase.PHASE4, Phase.PHASE5, Map.of(), constraintSet4, Set.of(), new float[]{});

        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE3)).thenReturn(null);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE4)).thenReturn(constraints4);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE5)).thenReturn(null);

        // Assert on constraints 3 -> 5, 4 -> 5, 5 -> 5
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE3)).isEqualTo(constraintSet4);
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE4)).isEqualTo(constraintSet4);
        assertThat(constraintService.impossibleToReachPhaseFiveConstraints(Phase.PHASE5)).isEqualTo(new HashSet<>());
    }

    @Test
    public void testPhaseConstraintMap() {
        PhaseTransitionConstraints constraints1 = new PhaseTransitionConstraints(Phase.PHASE1, Phase.PHASE2,
                Map.of(), Set.of(), Set.of(), new float[]{});
        PhaseTransitionConstraints constraints2 = new PhaseTransitionConstraints(Phase.PHASE2, Phase.PHASE3,
                Map.of(), Set.of(), Set.of(), new float[]{});
        PhaseTransitionConstraints constraints3 = new PhaseTransitionConstraints(Phase.PHASE3, Phase.PHASE4,
                Map.of(), Set.of(), Set.of(), new float[]{});
        PhaseTransitionConstraints constraints4 = new PhaseTransitionConstraints(Phase.PHASE4, Phase.PHASE5,
                Map.of(), Set.of(), Set.of(), new float[]{});
        PhaseTransitionConstraints constraints5 = new PhaseTransitionConstraints(Phase.PHASE5, null,
                Map.of(), Set.of(), Set.of(), new float[]{});

        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE1)).thenReturn(constraints1);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(constraints2);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE3)).thenReturn(constraints3);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE4)).thenReturn(constraints4);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE5)).thenReturn(constraints5);

        assertThat(constraintService.getPhaseConstraintMap()).isEqualTo(Map.of(Phase.PHASE1, constraints1,
                Phase.PHASE2, constraints2, Phase.PHASE3, constraints3, Phase.PHASE4, constraints4,
                Phase.PHASE5, constraints5));
    }

    @Test
    public void testGetPhaseImpossibleToReachEndMap() {
        PhaseTransitionConstraints constraints1 = new PhaseTransitionConstraints(Phase.PHASE1, Phase.PHASE2, Map.of(),
                Set.of(Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1))),
                Set.of(), new float[]{});
        PhaseTransitionConstraints constraints2 = new PhaseTransitionConstraints(Phase.PHASE2, Phase.PHASE3, Map.of(),
                Set.of(Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1))),
                Set.of(), new float[]{});
        PhaseTransitionConstraints constraints3 = new PhaseTransitionConstraints(Phase.PHASE3, Phase.PHASE4, Map.of(),
                Set.of(Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1))),
                Set.of(), new float[]{});
        PhaseTransitionConstraints constraints4 = new PhaseTransitionConstraints(Phase.PHASE4, Phase.PHASE5, Map.of(),
                Set.of(Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1))),
                Set.of(), new float[]{});
        PhaseTransitionConstraints constraints5 = new PhaseTransitionConstraints(Phase.PHASE5, null,
                Map.of(), Set.of(), Set.of(), new float[]{});

        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE1)).thenReturn(constraints1);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE2)).thenReturn(constraints2);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE3)).thenReturn(constraints3);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE4)).thenReturn(constraints4);
        when(mockConstraintProvider.getPhaseTransitionConstraints(Phase.PHASE5)).thenReturn(constraints5);

        Map<Phase, Set<Set<BeliefConstraint>>> map = constraintService.getPhaseImpossibleToReachEndMap();
        assertThat(map.get(Phase.PHASE1)).hasSize(4);
        assertThat(map.get(Phase.PHASE1).stream()).containsExactlyInAnyOrder(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(0), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1)));
        assertThat(map.get(Phase.PHASE2)).hasSize(3);
        assertThat(map.get(Phase.PHASE2).stream()).containsExactlyInAnyOrder(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(1), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1)));
        assertThat(map.get(Phase.PHASE3)).hasSize(2);
        assertThat(map.get(Phase.PHASE3).stream()).containsExactlyInAnyOrder(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(2), 1)),
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1)));
        assertThat(map.get(Phase.PHASE4)).hasSize(1);
        assertThat(map.get(Phase.PHASE4).stream()).containsExactlyInAnyOrder(
                Set.of(new BeliefConstraint(BoundaryCheck.EQ, utils.getBeliefOrdering().get(3), 1)));
        assertThat(map.get(Phase.PHASE5)).hasSize(0);
        assertThat(map.get(Phase.PHASE5).stream()).isEmpty();
    }
}
