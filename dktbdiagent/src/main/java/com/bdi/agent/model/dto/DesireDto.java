package com.bdi.agent.model.dto;

import com.bdi.agent.model.enums.DesireName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DesireDto {
    private DesireName name;

    @NotBlank
    private String fullName;

    @NotNull
    private Boolean isActive;
}
