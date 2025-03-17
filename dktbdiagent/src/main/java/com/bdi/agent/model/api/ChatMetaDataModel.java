package com.bdi.agent.model.api;

import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMetaDataModel {

    @NotBlank
    private Long conversationId;

    private String title;
    private LocalDateTime timestamp;

}
