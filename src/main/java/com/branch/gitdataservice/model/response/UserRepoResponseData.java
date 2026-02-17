package com.branch.gitdataservice.model.response;

import com.branch.gitdataservice.model.user.Repo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Schema
@Data
public class UserRepoResponseData {
        @JsonProperty("user_name")
        @Schema(description = "The username of the requested user")
        String userName;

        @Schema(description = "The user's preferred display name")
        @JsonProperty("display_name")
        String displayName;

        @Schema(description = "A url of the user's avatar")
        @JsonProperty("avatar")
        String avatar;

        @Schema(description = "The user's freely defined location")
        @JsonProperty("geo_location")
        String geoLocation;

        @Schema(description = "The email address tied to the user's github account")
        @JsonProperty("email")
        String email;

        @Schema(description = "The URL to the user's account")
        @JsonProperty("url")
        String url;

        @Schema(description = "The datetime the user's account was created at. ", format = "ddd, dd MMM yyyy HH:mm:ss GMT" ) //TODO: Add format
        @JsonProperty("created_at")
        String createdAt;

        @Schema(description = "A list of the user's public repositories")
        @JsonProperty("repos")
        List<Repo> repos;

}
