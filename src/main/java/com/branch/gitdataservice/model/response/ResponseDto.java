package com.branch.gitdataservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResponseDto<R> {
    @Schema(description = "Indicates the status of the quest")
    ResponseStatus status;

    @Schema(description = "The response data. Will only be populated in cases where status is 'SUCCESS' or 'PARTIAL'")
    R data;

    @Schema(description = "A list of any possible errors. Will only be populated in cases where status is 'SUCCESS' or 'PARTIAL")
    List<ResponseError> errors;
}
