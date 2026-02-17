package com.branch.gitdataservice.client;


import com.branch.gitdataservice.model.clients.github.responses.GithubUserRepoResponseData;
import com.branch.gitdataservice.model.clients.github.responses.GithubUserResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "GithubClient", url = "${clients.github.base-url}")
public interface GithubClient {

    @GetMapping(value = "${clients.github.user-url}", consumes = MediaType.APPLICATION_JSON_VALUE)
    GithubUserResponseData getUserData(@PathVariable("user") String userName);

    @GetMapping(value = "${clients.github.user-repo-url}", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<GithubUserRepoResponseData> getUserRepoData(@PathVariable("user") String userName);
}
