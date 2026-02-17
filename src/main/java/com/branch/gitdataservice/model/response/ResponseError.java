package com.branch.gitdataservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseError {
    @Schema(description = "The unique code for the error scenario")
    String code;

    @Schema(description = "The description of the error scenario")
    String description;
}
