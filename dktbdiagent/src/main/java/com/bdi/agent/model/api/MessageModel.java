package com.bdi.agent.model.api;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageModel {

    @NotBlank
    private String message;
    private boolean fromUser;

}
