package com.bdi.agent.model.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeliefDto {
    @NotBlank
    private String name;

    @NotBlank
    private String fullName;

    @Min(0)
    @Max(1)
    private Float value;
}
