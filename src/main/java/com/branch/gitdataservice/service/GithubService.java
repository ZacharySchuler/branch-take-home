package com.branch.gitdataservice.service;

import com.branch.gitdataservice.exception.UserNotFoundException;
import com.branch.gitdataservice.client.GithubClient;
import com.branch.gitdataservice.mapper.GithubMapper;
import com.branch.gitdataservice.model.clients.github.responses.GithubUserRepoResponseData;
import com.branch.gitdataservice.model.clients.github.responses.GithubUserResponseData;
import com.branch.gitdataservice.model.response.ResponseDto;
import com.branch.gitdataservice.model.response.ResponseError;
import com.branch.gitdataservice.model.response.ResponseStatus;
import com.branch.gitdataservice.model.response.UserRepoResponseData;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GithubService implements GitService {

    @Autowired
    GithubClient client;

    @Autowired
    GithubMapper mapper;

    @Cacheable(cacheNames = "user-repo-data", key = "#username", unless = "#result == null || #result.status.name() != 'SUCCESS'")
    public ResponseDto<UserRepoResponseData> fetchUserRepoData(String username, UUID traceId) throws UserNotFoundException, FeignException{
        log.debug("TraceId: {} - Cache Miss", traceId);

        GithubUserResponseData githubUserResponseData = null;

        try{
            githubUserResponseData  = client.getUserData(username);
        }catch(FeignException.FeignClientException.NotFound notFoundException){
            //Returning a 404
            log.error("TraceId: {} - FEIGN-CLIENT:USER: {}", traceId, notFoundException.getMessage());
            throw new UserNotFoundException();
        }
        List<GithubUserRepoResponseData> githubUserRepoResponseData = null;

        try{
            githubUserRepoResponseData = client.getUserRepoData(username);
        }catch(FeignException.FeignClientException.NotFound notFoundException){
            log.error("TraceId: {} - FEIGN-CLIENT:REPO: {}", traceId, notFoundException.getMessage());
            throw new UserNotFoundException();
        }catch(FeignException feignException){
            //Retuning a partial success (HTTP200) rather than an HTTP500 since we have some data
            log.error("TraceId: {} - FEIGN CLIENT:Repo: {}", traceId, feignException.getMessage());

            UserRepoResponseData userRepoResponseData = mapper.toUserRepoResponseData(githubUserRepoResponseData, githubUserResponseData);

            ResponseError error = new ResponseError("3", "Unable to fetch repos");
            return new ResponseDto<>(ResponseStatus.PARTIAL, userRepoResponseData, List.of(error));
        }

        // Run the mapper

        UserRepoResponseData userRepoResponseData = mapper.toUserRepoResponseData(githubUserRepoResponseData, githubUserResponseData);
        //Return

        return new ResponseDto<>(ResponseStatus.SUCCESS, userRepoResponseData, null);
    }
}
