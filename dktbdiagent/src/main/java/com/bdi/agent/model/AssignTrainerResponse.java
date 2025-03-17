package com.bdi.agent.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class AssignTrainerResponse {

    @Getter
    private Long userId;

    @Getter
    private String username;

    @Getter
    private boolean userExists; // If the user with this id exists

    @Getter
    private boolean userIsTrainer; // If the user with this id is of role type TRAINER
}