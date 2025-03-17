package com.bdi.agent.service.graph;

import com.bdi.agent.model.dto.BeliefDto;
import com.bdi.agent.model.dto.DesireDto;
import com.bdi.agent.model.enums.BeliefName;
import com.bdi.agent.model.enums.BoundaryCheck;
import com.bdi.agent.model.enums.DesireName;
import com.bdi.agent.model.enums.Phase;
import com.bdi.agent.model.graph.GraphEdge;
import com.bdi.agent.service.ConstraintService;
import com.bdi.agent.utils.FloatComparer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GraphUtilsService {

    @Value("${minValue}")
    private float minValue;
    @Value("${maxValue}")
    private float maxValue;
    @Value("${relatednessBeliefs}")
    private BeliefName[] relatednessBeliefs;

    private final List<BeliefName> beliefOrdering = Arrays.asList(BeliefName.values());

    private final FloatComparer floatComparer;
    private final ConstraintService constraintService;

    /**
     * Creates a GraphBeliefDesireService.
     *
     * @param floatComparer         The floatComparer, used to safely compare float values.
     * @param constraintService     The constraintService, used to check desire constraints.
     */
    @Autowired
    public GraphUtilsService(FloatComparer floatComparer, ConstraintService constraintService) {
        this.floatComparer = floatComparer;
        this.constraintService = constraintService;
    }

    /**
     * Creates a list of BeliefDtos, given the array of values.
     * If there are less belief names than values, returns null.
     *
     * @param beliefs The array of belief values. If there are less
     *                belief names than values, returns null.
     * @return A list of BeliefDtos, given the array of values.
     */
    public List<BeliefDto> beliefArrayToDtoList(float[] beliefs) {
        if (beliefs.length > BeliefName.values().length) {
            return null;
        }

        List<BeliefDto> beliefDtos = new ArrayList<>();

        for (int i = 0; i < beliefs.length; i++) {
            beliefDtos.add(new BeliefDto(
                    BeliefName.values()[i].name(),
                    getFullBeliefName(BeliefName.values()[i]),
                    beliefs[i]));
        }

        return beliefDtos;
    }

    /**
     * Gets the DesireDto list generated from the array of belief values.
     *
     * @param beliefs The array of belief values.
     * @return The DesireDto list generated from the array of belief values.
     */
    public List<DesireDto> getDesiresList(float[] beliefs) {
        List<DesireDto> desires = new ArrayList<>();

        desires.add(new DesireDto(DesireName.D1, getFullDesireName(DesireName.D1),
                constraintService.checkDesireConstraints(DesireName.D1, beliefs)));
        desires.add(new DesireDto(DesireName.D2, getFullDesireName(DesireName.D2),
                constraintService.checkDesireConstraints(DesireName.D2, beliefs)));
        desires.add(new DesireDto(DesireName.D3, getFullDesireName(DesireName.D3),
                constraintService.checkDesireConstraints(DesireName.D3, beliefs)));
        desires.add(new DesireDto(DesireName.D4, getFullDesireName(DesireName.D4),
                constraintService.checkDesireConstraints(DesireName.D4, beliefs)));
        desires.add(new DesireDto(DesireName.D5, getFullDesireName(DesireName.D5),
                constraintService.checkDesireConstraints(DesireName.D5, beliefs)));

        return desires;
    }

    /**
     * Gets the full name of the desire, given the basic name abbreviation.
     *
     * @param desireName    The DesireName.
     * @return The full name of the desire.
     */
    public String getFullDesireName(DesireName desireName) {
        // TODO Could be moved to a more fitting class, to load static data.

        return switch (desireName) {
            case D1 -> "Lilobot wil het over zijn probleem hebben";
            case D2 -> "Lilobot wil het gesprek beëindigen";
            case D3 -> "Lilobot wil dat de Kindertelefoon de pestkoppen van school haalt";
            case D4 -> "Lilobot wil met zijn leraar praten over situatie";
            case D5 -> "Lilobot wil samen met de Kindertelefoon een oplossing zoeken,";
            case D6 -> "Lilobot is happy to end the conv (TBD),";
        };
    }

    /**
     * Gets the full name of the belief, given the basic name abbreviation.
     *
     * @param name The basic name abbreviation of the belief.
     * @return The full name of the belief.
     */
    public String getFullBeliefName(BeliefName name) {
        // TODO Could be moved to a more fitting class, to load static data.

        return switch (name) {
            case B1 -> "Ik voel me in controle in het gesprek";
            case B2 -> "Ik denk dat ik competent ben om het probleem op te lossen";
            case B3 -> "Ik voel me verbonden met KT";
            case B4 -> "Ik denk dat KT te vertrouwen is";
            case B5 -> "Ik denk dat KT mij begrijpt";
            case B6 -> "Ik denk dat KT geïnteresseerd is in mijn verhaal";
            case B7 -> "Ik denk dat KT mij kan helpen";
            case B8 -> "Ik denk dat KT het probleem kan oplossen";
            case B9 -> "Ik denk dat ik mijn verhaal heb verteld";
            case B10 -> "Ik denk dat KT vraagt naar een wens";
            case B11 -> "Ik denk dat KT vraagt naar een positieve wens";
            case B12 -> "Ik denk dat KT vraagt naar een vertrouwenspersoon";
            case B13 -> "Ik denk dat Juf Ellie mij kan helpen";
            case B14 -> "Ik voel me veilig in het gesprek";
            case B15 -> "Ik denk dat KT wil het gesprek beëindigen";
            case B16 -> "Ik denk dat KT en ik samen tot een oplossing zullen komen";
            case B17 -> "Ik denk dat KT het probleem voor mij gaat oplossen";
        };
    }

    /**
     * Check if the belief value needs to be increased.
     *
     * @param currentValue      The current value.
     * @param goalValue         The goal value.
     * @param boundary          The boundary specification.
     * @return Whether the belief value needs to be increased.
     */
    public boolean needToIncrease(float currentValue, float goalValue, BoundaryCheck boundary) {
        // If it should be >=, but is <, need to increase
        if (boundary == BoundaryCheck.GEQ || boundary == BoundaryCheck.EQ) {
            return floatComparer.lessThan(currentValue, goalValue);
        }

        // If it should be >, but is <=, need to increase
        if (boundary == BoundaryCheck.GT) {
            return floatComparer.lessOrEqual(currentValue, goalValue);
        }

        return false;
    }

    /**
     * Check if the method should not update the belief anymore, i.e., if the goal condition is fulfilled.
     *
     * @param currentValue  The current value of the belief.
     * @param goalValue     The goal value of the belief.
     * @param boundary      Whether the stop condition is <=, >=, <, >.
     * @return True, if it should not update, and false if it should continue updating the belief.
     */
    public boolean stopUpdating(float currentValue, float goalValue, BoundaryCheck boundary) {
        // Check if == leads to stop
        boolean equalsStop = (boundary == BoundaryCheck.LEQ || boundary == BoundaryCheck.GEQ
                || boundary == BoundaryCheck.EQ)
                && floatComparer.equalTo(currentValue, goalValue);
        // Check if < leads to stop
        boolean lessThanStop = (boundary == BoundaryCheck.LEQ || boundary == BoundaryCheck.LT)
                && floatComparer.lessThan(currentValue, goalValue);
        // Check if > leads to stop
        boolean greaterThanStop = (boundary == BoundaryCheck.GEQ || boundary == BoundaryCheck.GT)
                && floatComparer.greaterThan(currentValue, goalValue);

        return equalsStop || lessThanStop || greaterThanStop;
    }

    /**
     * Calculates the relatedness for B3, given the beliefs, and the relatednessBeliefs specified as an
     * attribute to this class.
     *
     * @param beliefs The belief values.
     * @return The relatedness score, which is the average value of the relatednessBeliefs.
     */
    public float calculateRelatedness(float[] beliefs) {
        float sum = 0;

        for (BeliefName belief : relatednessBeliefs) {
            sum += getBeliefValue(belief, beliefs);
        }

        return sum / relatednessBeliefs.length;
    }

    /**
     * Gets value of the given belief from the array.
     *
     * @param name      The name of the belief.
     * @param beliefs   The belief array.
     * @return The value of the belief.
     */
    public float getBeliefValue(BeliefName name, float[] beliefs) {
        return beliefs[beliefOrdering.indexOf(name)];
    }

    /**
     * Sets value of the given belief from the array.
     *
     * @param name      The name of the belief.
     * @param beliefs   The belief array.
     * @param value     The value to set to.
     */
    public void setBeliefValue(BeliefName name, float[] beliefs, float value) {
        beliefs[beliefOrdering.indexOf(name)] = value;
    }

    /**
     * Increase value of the given belief from the array, if the previous value is < maxValue.
     * The given value also needs to be positive.
     *
     * @param name      The name of the belief.
     * @param beliefs   The belief array.
     * @param value     The value by which to increase.
     */
    public void increaseBeliefValue(BeliefName name, float[] beliefs, float value) {
        // This is handled in a similar manner as in the DesireService
        if (floatComparer.greaterOrEqual(beliefs[beliefOrdering.indexOf(name)], maxValue)
                || floatComparer.lessThan(value, 0)) {
            return;
        }

        beliefs[beliefOrdering.indexOf(name)] += value;
    }

    /**
     * Decrease value of the given belief from the array, if the previous value is > minValue.
     * The given value also needs to be positive.
     *
     * @param name      The name of the belief.
     * @param beliefs   The belief array.
     * @param value     The value by which to decrease.
     */
    public void decreaseBeliefValue(BeliefName name, float[] beliefs, float value) {
        // This is handled in a similar manner as in the DesireService
        if (floatComparer.lessOrEqual(beliefs[beliefOrdering.indexOf(name)], minValue)
                || floatComparer.lessThan(value, 0)) {
            return;
        }

        beliefs[beliefOrdering.indexOf(name)] -= value;
    }

    /**
     * Gets the active desire (DesireName), given the values of the beliefs.
     *
     * @param beliefs The array of belief values.
     * @return The name of the active desire.
     */
    public DesireName getActiveDesire(float[] beliefs) {
        DesireName[] desires = DesireName.values();
        for (DesireName desire : desires) {
            boolean value = constraintService.checkDesireConstraints(desire, beliefs);

            if (value) {
                return desire;
            }
        }

        return null;
    }

    /**
     * Maps the desire to its phase.
     *
     * @param desire The desire.
     * @return The phase of the Five Phase Model which the desire corresponds to.
     */
    public Phase getPhaseByDesire(DesireName desire) {
        // TODO this could be loaded by a configuration file

        if (desire == null) {
            return Phase.PHASE1;
        }

        return switch (desire) {
            case D1 -> Phase.PHASE2;
            case D2 -> Phase.PHASE5;
            case D5, D3 -> Phase.PHASE3;
            case D4, D6 -> Phase.PHASE4;
        };
    }

    /**
     * Retrieve the desires which, if one of them is the active desire, would make the agent be in the given phase.
     *
     * @param phase The phase for which to get the desires.
     * @return  The desires (DesireNames) which, if one of them is the active desire, would make the agent be in
     *          the given phase. They are sorted ascendingly.
     */
    public List<DesireName> getDesiresByPhase(Phase phase) {
        // TODO this could be loaded by a configuration file

        if (phase == null) {
            return null;
        }

        return switch (phase) {
            case PHASE1 -> List.of();
            case PHASE2 -> List.of(DesireName.D1);
            case PHASE3 -> List.of(DesireName.D3, DesireName.D5);
            case PHASE4 -> List.of(DesireName.D4, DesireName.D6);
            case PHASE5 -> List.of(DesireName.D2);
        };
    }

    /**
     * Gets an example message for a given edge.
     *
     * @param edge The edge for which to generate the example message.
     * @return The example message for the intention of the edge.
     */
    public String getExampleMessage(GraphEdge edge) {
        // TODO Could be moved to a more fitting class, to load static data. Also, may be nice to choose
        //       a random message from the nlu instead.

        return switch (edge.getIntentionName()) {
            case "request_chitchat_greeting" -> "hello";
            case "request_chitchat_faring" -> "How are you?";
            case "trigger_unknown_what" -> "What do you want to talk about?";
            case "request_bullying_who" -> "who is bullying you?";
            case "request_unknown_who" -> "who is bullying you?";
            case "request_bullying_details" -> "Would you like to tell me more about your situation?";
            case "request_unknown_details" -> "what exactly did they do?";
            case "request_bullying_count" -> "How many kids bullied you?";
            case "request_bullying_location" -> "where do they bully you?";
            case "request_unknown_location" -> "where does this happen?";
            case "request_bullying_duration" -> "when did the bullying start?";
            case "request_unknown_duration" -> "how long has this been going on?";
            case "request_bullying_frequency" -> "do they bully you often?";
            case "request_unknown_frequency" -> "do they do that often?";
            case "request_unknown_when" -> "when did that happen?";
            case "request_bullying_why" -> "why did they bully you?";
            case "request_unknown_why" -> "do you know why they do this?";
            case "request_bullying_response" -> "what do you do when they bully you?";
            case "request_unknown_response" -> "what do you do when this happens?";
            case "request_unknown_feeling" -> "how does that make you feel?";
            case "request_bullying_confidant" -> "Did you tell anyone about the bullying?";
            case "request_unknown_confidant" -> "have you told anyone about this?";
            case "request_bullying_parent" -> "did you tell your parents about this?";
            case "confirm_bullying_summary" -> "so, you were bullied at school yesterday, is that right?";
            case "request_school_start" -> "did you start at this school this year?";
            case "inform_unknown_positive" -> "I can do that";
            case "inform_unknown_negative" -> "Unfortunately, that's not possible";
            case "inform_goal_negative" -> "the child helpline can not call your school";
            case "inform_goal_help" -> "I can talk to you";
            case "ack_unknown_empathize" -> "oh how annoying";
            case "ack_unknown_neutral" -> "that's good to hear";
            case "ack_unknown_compliment" -> "That's a good idea";
            case "ack_contactingkt_compliment" -> "Good that you're contacting the child helpline";
            case "request_goal_what" -> "what is your goal for contacting the children helpline?";
            case "request_goal_dream" -> "suppose you wake up tomorrow and the problem is no longer there, what would be different?";
            case "request_goal_feeling" -> "what would it feel like if the bullying stops?";
            case "confirm_goal_summary" -> "if I understand you correctly, you are being bullied at school and you want it to stop, is that correct?";
            case "confirm_goal_collaborate" -> "we can look for a different solution together. what do you think of that?";
            case "request_goal_howkt" -> "is there anything I can do to help you?";
            case "request_goal_howchild" -> "what could you do yourself?";
            case "request_unknown_how" -> "what can you do to achieve this?";
            case "request_confidant_who" -> "is there anyone you trust?";
            case "confirm_confidant_teacher" -> "would you try to talk to your teacher?";
            case "confirm_confidant_parent" -> "could you talk to your parents about this?";
            case "request_confidant_when" -> "when could you talk to your teacher?";
            case "request_confidant_feeling" -> "what would it be like to talk to Miss Ellie?";
            case "request_confidant_why" -> "why are you afraid to talk to your teacher?";
            case "request_confidant_how" -> "how would you discuss it with your teacher?";
            case "inform_confidant_help" -> "she can listen to you and support you";
            case "inform_confidant_say" -> "you can tell her the same thing you told me";
            case "request_confidant_say" -> "what would you like to tell Miss Ellie?";
            case "confirm_confidant_summary" -> "Shall we agree that you will talk to your teacher about your situation?";
            case "request_chitchat_end" -> "I suggest we wrap up the conversation";
            case "confirm_chitchat_satisfaction" -> "did you like this conversation?";
            case "request_chitchat_goodbye" -> "bye bye";
            case "ack_unknown_guilt" -> "it's all your fault";
            case "ack_unknown_taunt" -> "you're so stupid";
            default -> "undefined";
        };
    }
}
