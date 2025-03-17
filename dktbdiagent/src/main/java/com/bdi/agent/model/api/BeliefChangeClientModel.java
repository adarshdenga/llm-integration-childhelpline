package com.bdi.agent.model.api;


import com.bdi.agent.model.enums.BeliefUpdateType;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeliefChangeClientModel {

    @NotBlank
    private String belief;
    private float value;
    private String msgText;
    private int logIndex;
    private Boolean isManualUpdate;
    private BeliefUpdateType beliefUpdateType;

}
