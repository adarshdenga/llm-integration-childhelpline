package com.bdi.agent.exceptions;

/**
 * Exception thrown when a trainer sends a join request to a learner
 * without being their assigned trainer.
 */
public class InvalidJoinRequest extends Exception {

    static final long serialVersionUID = -3387516993124229948L;

    public InvalidJoinRequest(String message) {
        super(message);
    }

}

