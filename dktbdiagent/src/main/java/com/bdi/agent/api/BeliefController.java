package com.bdi.agent.api;

import com.bdi.agent.model.Agent;
import com.bdi.agent.model.Belief;
import com.bdi.agent.model.ExceptionalBelief;
import com.bdi.agent.model.api.BeliefChangeModel;
import com.bdi.agent.model.api.InitialBeliefModel;
import com.bdi.agent.model.api.PhaseChangeRequest;
import com.bdi.agent.service.AgentService;
import com.bdi.agent.service.BeliefService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("beliefs")
public class BeliefController {

    private final AgentService agentService;
    private final BeliefService beliefService;

    /**
     * Instantiates a new Belief Controller.
     *
     * @param agentService the agentService for accessing agents.
     * @param beliefService the beliefService for accessing beliefs.
     */
    @Autowired
    public BeliefController(AgentService agentService, BeliefService beliefService) {
        this.agentService = agentService;
        this.beliefService = beliefService;
    }

    /**
     * Updates the value of a specific belief for an agent.
     *
     * @param conversationId the conversation ID to be used to retrieve the agent.
     * @param beliefChangeRequest the belief to be changed
     * @return 200 OK
     */
    @PutMapping("/update/{conversationId}")
    public ResponseEntity updateBelief(@PathVariable("conversationId") String conversationId,
                                       @RequestBody BeliefChangeModel beliefChangeRequest) {
        try {
            agentService.updateBelief(conversationId, beliefChangeRequest.getBelief(), beliefChangeRequest.getValue());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Fetches all belief values for an agent.
     *
     * @param conversationId the conversation ID to be used to retrieve the agent.
     * @return 200 OK with the list of all beliefs as BeliefChangeModel objects if successful, Bad Request otherwise.
     */
    @CrossOrigin("*")
    @GetMapping("/all/{conversationId}")
    public ResponseEntity getAllBeliefs(@PathVariable("conversationId") String conversationId) {
        try {
            Agent agent = agentService.getByUserId(conversationId);
            return new ResponseEntity<>(beliefService.getByAgent(agent.getId())
                    .stream()
                    .map(belief -> new BeliefChangeModel(belief.getName(), belief.getValue()))
                    .toArray(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Fetches all initial belief values. These are the beliefs as seen in the beliefs.csv file. This method is used
     * to fetch all beliefs for the Training Portal, to ensure beliefs are shown even if the conversation didn't get
     * initialized yet on the backend.
     *
     * @return 200 OK with the list of all beliefs as InitialBeliefModel objects if successful, Bad Request otherwise.
     */
    @CrossOrigin("*")
    @GetMapping("/all")
    public ResponseEntity getAllInitialBeliefsFromCsv() {
        try {
            List<Belief> beliefs = beliefService.getAllInitialBeliefsSorted();

            return new ResponseEntity<>(beliefs
                    .stream()
                    .map(b -> {
                        if (b instanceof ExceptionalBelief) {
                            ExceptionalBelief eb = (ExceptionalBelief) b;
                            return new InitialBeliefModel(b.getName(), b.getValue(), b.getFullName(),
                                    false,
                                    eb.getExceptionalReason()
                            );
                        } else {
                            return new InitialBeliefModel(b.getName(), b.getValue(), b.getFullName());
                        }
                    })
                    .toArray(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Sets the given agents phase to a specified one, using exemplary belief values to switch to the phase.
     *
     * @param phaseChangeRequest A model specifying the agent, and the phase
     * @return 200 OK with the list of all belief change objects if successful, Bad Request otherwise.
     */
    @CrossOrigin("*")
    @PutMapping("/phase")
    public ResponseEntity changeAgentToPhase(@RequestBody PhaseChangeRequest phaseChangeRequest) {
        try {
            List<BeliefChangeModel> beliefChanges = agentService.setAgentStateToPhase(phaseChangeRequest.getSessionId(),
                    phaseChangeRequest.getPhase());

            return new ResponseEntity<>(beliefChanges.toArray(), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
