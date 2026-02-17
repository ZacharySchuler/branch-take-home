package com.branch.gitdataservice.controller;

import com.branch.gitdataservice.exception.UserNotFoundException;
import com.branch.gitdataservice.model.response.ResponseDto;
import com.branch.gitdataservice.model.response.ResponseError;
import com.branch.gitdataservice.model.response.ResponseStatus;
import com.branch.gitdataservice.model.response.UserRepoResponseData;
import com.branch.gitdataservice.service.GithubService;
import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/api/v1/git-data/users")
@Slf4j
public class UserController {

	@Autowired
	GithubService githubService;

	@Operation(
			summary = "Fetches the public git repos for a user",
			responses = {
					@ApiResponse(responseCode = "200", description = "User and their repo's successfully fetched. Status = SUCCESS or Partial. Data is populated. Error is populated if Status = Partial"),
					@ApiResponse(responseCode = "404", description = "Username not found - Status = ERROR, data is null, error is populated"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error - Status = ERROR, data is null, error is populated")
			}
	)
	@GetMapping("/{userName}/repos")
	public ResponseEntity<ResponseDto<UserRepoResponseData>> fetchUserRepos(
			@PathVariable String userName
	) {
		UUID traceId = UUID.randomUUID();
		log.info("Enter::: /{}/repos - traceId::{}", userName, traceId);

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Trace-Id", traceId.toString());

		ResponseDto<UserRepoResponseData> serviceResponse;

		ResponseEntity<ResponseDto<UserRepoResponseData>> controllerResponse;

		try{
			serviceResponse = githubService.fetchUserRepoData(userName, traceId);

			controllerResponse = ResponseEntity.status(200).headers(responseHeaders).body(serviceResponse);
		} catch(UserNotFoundException neuException){
			log.error("TraceId: {} - User not found: {}", traceId, userName);
			ResponseError error = new ResponseError("1", "User not found");
			controllerResponse =  ResponseEntity.status(404)
					.headers(responseHeaders)
					.body(new ResponseDto<>(ResponseStatus.ERROR, null, List.of(error)));
		} catch(FeignException feignException){
			ResponseError error = new ResponseError("2", "Downstream API Error"); // Keeping generic to prevent data leaking
			controllerResponse =  ResponseEntity.status(500)
					.headers(responseHeaders)
					.body(new ResponseDto<>(ResponseStatus.ERROR, null, List.of(error)));
			log.error("TraceId: {} - FEIGN-CLIENT: {}", traceId, feignException.getMessage());
		}
		log.info("Exit::: /{}/repos - traceId::{} - status::{}",userName, traceId, controllerResponse.getStatusCode());
		return controllerResponse;
	}
}
