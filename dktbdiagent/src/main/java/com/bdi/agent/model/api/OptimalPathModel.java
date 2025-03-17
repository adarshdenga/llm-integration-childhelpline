package com.bdi.agent.model.api;

import com.bdi.agent.model.dto.MessageNodeDto;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OptimalPathModel {
    @NotNull
    private List<MessageNodeDto> nodes;
}
