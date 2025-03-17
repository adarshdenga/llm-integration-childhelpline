package com.bdi.agent.model.dto;

import com.bdi.agent.model.enums.Phase;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageNodeDto {
    @NotNull
    private List<BeliefDto> beliefs;

    @NotNull
    private List<DesireDto> desires;

    @NotNull
    private Phase phase;

    // This is the edge, should be null iff this is the last message node in the path
    private IntentDto edge;
}
