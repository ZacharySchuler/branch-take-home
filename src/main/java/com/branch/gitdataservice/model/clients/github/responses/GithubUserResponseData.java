package com.branch.gitdataservice.model.clients.github.responses;

import lombok.Data;

import java.time.Instant;

@Data
public class GithubUserResponseData {

    String login;
    String name;
    String avatar_url;
    String location;
    String email;
    String url;
    Instant created_at; //TODO: Check Dates
}
