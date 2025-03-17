package com.bdi.agent.model.api;

import com.bdi.agent.model.enums.Phase;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PhaseChangeRequest {
    @NotNull
    private String sessionId;

    @NotNull
    private Phase phase;
}
