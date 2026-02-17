package com.branch.gitdataservice.controller

import com.branch.gitdataservice.exception.UserNotFoundException
import com.branch.gitdataservice.model.response.ResponseDto
import com.branch.gitdataservice.model.response.ResponseStatus
import com.branch.gitdataservice.model.response.UserRepoResponseData
import com.branch.gitdataservice.model.user.Repo
import com.branch.gitdataservice.service.GithubService
import feign.FeignException
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
class UserControllerTest extends Specification {

    GithubService githubService = Mock()
    UserController userController = new UserController()
    MockMvc mockMvc
    def responseTraceIdHeader = "Trace-Id"
    FeignException feignException = Mock(FeignException)

    def setup() {
        ReflectionTestUtils.setField(userController, "githubService", githubService)
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build()
    }

    def "service returns success payload | HTTP200"() {
        given: "The service call is successful and returns a response of HTTP200"
        def data = new UserRepoResponseData()
        data.userName = "octocat"
        data.displayName = "The Octocat"
        data.avatar = "https://avatars.githubusercontent.com/u/583231?v=4"
        data.geoLocation = "San Francisco"
        data.email = null
        data.url = "https://api.github.com/users/octocat"
        data.createdAt = "Tue, 25 Jan 2011 18:44:36 GMT"
        data.repos = [new Repo("Hello-World", "https://api.github.com/repos/octocat/Hello-World")]

        def serviceResponse = new ResponseDto<UserRepoResponseData>(ResponseStatus.SUCCESS, data, null)

        githubService.fetchUserRepoData(*_) >> serviceResponse

        when: "We the controller receives the call"
        def result = mockMvc.perform(get("/api/v1/git-data/users/octocat/repos"))

        then: "The Response should be an http200 and the status is successful"
        result.andExpect(status().isOk())
    }

    def "Controller catches a UserNotFoundException exception | 404"() {
        given: "The service call is successful and returns a response of HTTP200"

        githubService.fetchUserRepoData(*_) >> {{throw new UserNotFoundException()}}

        when: "We the controller receives the call"
        def result = mockMvc.perform(get("/api/v1/git-data/users/octocat/repos"))

        then: "The Response should be an http200 and the status is successful"
        result.andExpect(status().is(404))
                .andExpect(jsonPath('$.status').value('ERROR'))
                .andExpect(header().exists(responseTraceIdHeader))
    }

    def "Controller catches a feignException | 500"() {
        given: "The service call is successful and returns a response of HTTP200"

        githubService.fetchUserRepoData(*_) >> { throw feignException }

        when: "We the controller receives the call"
        def result = mockMvc.perform(get("/api/v1/git-data/users/octocat/repos"))

        then: "The Response should be an http200 and the status is successful"
        result.andExpect(status().is(500))
                .andExpect(jsonPath('$.status').value('ERROR'))
                .andExpect(header().exists(responseTraceIdHeader))
    }

    def "fetchUserRepos returns 404 due to nonexistant uri"() {
        given: "A user calls the controller with an incorrect uri"
        def incorrectUri = "/api/v1/git-data/users/octocat/repos/commits"

        when: "The call happens"
        def result = mockMvc.perform(get(incorrectUri))

        then: "An Http404 should be returned"
        result.andExpect(status().is(404))
    }

}
