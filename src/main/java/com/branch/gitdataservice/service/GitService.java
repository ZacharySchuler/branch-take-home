package com.branch.gitdataservice.service;

import com.branch.gitdataservice.exception.UserNotFoundException;
import com.branch.gitdataservice.model.response.ResponseDto;
import com.branch.gitdataservice.model.response.UserRepoResponseData;
import feign.FeignException;

import java.util.UUID;

public interface GitService {

    /*
    This method is to fetch a user's profile information and all of their repos.

    It's expected that a UserNotFoundException exception will be return as an HTTP404
    Its expected that a FeignException will be returned as an HTTP500
     */
    ResponseDto<UserRepoResponseData> fetchUserRepoData(String username, UUID traceId) throws UserNotFoundException, FeignException;
}
