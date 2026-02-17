package com.branch.gitdataservice.service

import com.branch.gitdataservice.exception.UserNotFoundException
import com.branch.gitdataservice.client.GithubClient
import com.branch.gitdataservice.mapper.GithubMapper
import com.branch.gitdataservice.model.clients.github.responses.GithubUserRepoResponseData
import com.branch.gitdataservice.model.clients.github.responses.GithubUserResponseData
import com.branch.gitdataservice.model.response.ResponseStatus
import com.branch.gitdataservice.model.response.UserRepoResponseData
import feign.FeignException
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class GithubServiceTest extends Specification {

    GithubService githubService = new GithubService()
    GithubClient githubClient = Mock()
    GithubMapper githubMapper = Mock()

    def setup() {
        ReflectionTestUtils.setField(githubService, "client", githubClient)
        ReflectionTestUtils.setField(githubService, "mapper", githubMapper)
    }

    def "fetchUserRepoData| Success scenario"() {
        given: "a valid user and repos are returned by github"
        def username = "octocat"
        def traceId = UUID.randomUUID()
        def githubUser = new GithubUserResponseData(login: "octocat", name: "The Octocat")
        def githubRepos = [new GithubUserRepoResponseData(name: "Hello-World", url: "https://api.github.com/repos/octocat/Hello-World")]
        def mappedResponse = new UserRepoResponseData(userName: "octocat")

        githubClient.getUserData(username) >> githubUser
        githubClient.getUserRepoData(username) >> githubRepos
        githubMapper.toUserRepoResponseData(githubRepos, githubUser) >> mappedResponse

        when: "the service is invoked"
        def result = githubService.fetchUserRepoData(username, traceId)

        then: "a SUCCESS response is returned with mapped data and no errors"
        result.status == ResponseStatus.SUCCESS
        result.data == mappedResponse
        result.errors == null
    }

    def "fetchUserRepoData| throws UserNotFoundException when user lookup returns 404"() {
        given: "github returns 404 for user data"
        def username = "missing-user"
        def traceId = UUID.randomUUID()
        def notFound = Mock(FeignException.FeignClientException.NotFound)

        githubClient.getUserData(username) >> { throw notFound }

        when: "the service is invoked"
        githubService.fetchUserRepoData(username, traceId)

        then: "the domain UserNotFoundException exception is thrown"
        thrown(UserNotFoundException)
    }

    def "fetchUserRepoData| throws UserNotFoundException when repo lookup returns 404"() {
        given: "user exists but repo endpoint returns 404"
        def username = "octocat"
        def traceId = UUID.randomUUID()
        def githubUser = new GithubUserResponseData()
        def notFound = Mock(FeignException.FeignClientException.NotFound)

        githubClient.getUserData(username) >> githubUser
        githubClient.getUserRepoData(username) >> { throw notFound }

        when: "the service is invoked"
        githubService.fetchUserRepoData(username, traceId)

        then: "the domain UserNotFoundException exception is thrown"
        thrown(UserNotFoundException)
    }

    def "fetchUserRepoData | returns PARTIAL when repo lookup throws non-404 feign exception"() {
        given: "user exists but repo fetch fails with a non-404 feign exception"
        def username = "octocat"
        def traceId = UUID.randomUUID()
        def githubUser = new GithubUserResponseData(login: "octocat", name: "The Octocat")
        def feignException = Mock(FeignException)
        def mappedResponse = new UserRepoResponseData(userName: "octocat")

        githubClient.getUserData(username) >> githubUser
        githubClient.getUserRepoData(username) >> { throw feignException }
        githubMapper.toUserRepoResponseData(null, githubUser) >> mappedResponse

        when: "the service is invoked"
        def result = githubService.fetchUserRepoData(username, traceId)

        then: "a PARTIAL response is returned with mapped partial data and one error"
        result.status == ResponseStatus.PARTIAL
        noExceptionThrown()
    }

    def "fetchUserRepoData | throws FeignException when user lookup client throws non-400 feign exception"(){
        given: "github throws a feign exception for user data"
        def username = "octocat"
        def traceId = UUID.randomUUID()
        def feignException = Mock(FeignException)

        githubClient.getUserData(username) >> { throw feignException }

        when: "the service is invoked"
        githubService.fetchUserRepoData(username, traceId)

        then: "the domain UserNotFoundException exception is thrown"
        thrown(FeignException)
    }

}
