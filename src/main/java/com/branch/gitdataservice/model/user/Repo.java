package com.branch.gitdataservice.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema
@Data
@AllArgsConstructor
public class Repo {

    @Schema(description = "The user defined name of the repo")
    String name;

    @Schema(description = "The url to the repo")
    String url;
}
