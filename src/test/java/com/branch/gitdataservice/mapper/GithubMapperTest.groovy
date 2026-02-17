package com.branch.gitdataservice.mapper

import com.branch.gitdataservice.model.clients.github.responses.GithubUserRepoResponseData
import com.branch.gitdataservice.model.clients.github.responses.GithubUserResponseData
import org.mapstruct.factory.Mappers
import spock.lang.Specification

import java.time.Instant

class GithubMapperTest extends Specification {

    GithubMapper mapper = Mappers.getMapper(GithubMapper)

    def "toUserRepoResponseData maps github user and repos into response dto"() {
        given: "a github user payload and repository list"
        def user = new GithubUserResponseData(
                login: "octocat",
                name: "The Octocat",
                avatar_url: "https://avatars.githubusercontent.com/u/583231?v=4",
                location: "San Francisco",
                email: "octocat@github.com",
                url: "https://api.github.com/users/octocat",
                created_at: Instant.parse("2011-01-25T18:44:36Z")
        )
        def repos = [
                new GithubUserRepoResponseData(name: "Hello-World", url: "https://api.github.com/repos/octocat/Hello-World"),
                new GithubUserRepoResponseData(name: "Foo-Bar", url: "https://api.github.com/repos/octocat/Foo-Bar")
        ]

        when: "the mapper converts the payload"
        def result = mapper.toUserRepoResponseData(repos, user)

        then: "all mapped fields match expected values"
        result.userName == "octocat"
        result.displayName == "The Octocat"
        result.avatar == "https://avatars.githubusercontent.com/u/583231?v=4"
        result.geoLocation == "San Francisco"
        result.email == "octocat@github.com"
        result.url == "https://api.github.com/users/octocat"
        result.createdAt == "Tue, 25 Jan 2011 18:44:36 GMT"
        result.repos*.name == ["Hello-World", "Foo-Bar"]
        result.repos*.url == [
                "https://api.github.com/repos/octocat/Hello-World",
                "https://api.github.com/repos/octocat/Foo-Bar"
        ]
    }

    def "mapRepoToList filters null entries and maps valid repos"() {
        given: "a list with valid and null repo entries"
        def repos = [
                new GithubUserRepoResponseData(name: "Hello-World", url: "https://api.github.com/repos/octocat/Hello-World"),
                null,
                new GithubUserRepoResponseData(name: "Foo-Bar", url: "https://api.github.com/repos/octocat/Foo-Bar")
        ]

        when: "repo list mapping is invoked"
        def result = mapper.mapRepoToList(repos)

        then: "only non-null entries are mapped"
        result.size() == 2
        result*.name == ["Hello-World", "Foo-Bar"]
    }

    def "formatInstantToRFC_1123 returns null when instant is null"() {
        given: "a null instant"
        Instant createdAt = null

        when: "date formatting is invoked"
        def result = mapper.formatInstantToRFC_1123(createdAt)

        then: "the mapper returns null"
        result == null
    }

    def "toRepo maps name and url"() {
        given: "a github repo object"
        def repo = new GithubUserRepoResponseData(name: "Hello-World", url: "https://api.github.com/repos/octocat/Hello-World")

        when: "the mapper converts it"
        def result = mapper.toRepo(repo)

        then: "both fields are mapped"
        result.name == "Hello-World"
        result.url == "https://api.github.com/repos/octocat/Hello-World"
    }

    def "Partial Data Scenario: Null Repos, Existing user"() {
        given: "a valid github user and null repos list"
        def user = new GithubUserResponseData(
                login: "octocat",
                name: "The Octocat",
                avatar_url: "https://avatars.githubusercontent.com/u/583231?v=4",
                location: "San Francisco",
                email: "octocat@github.com",
                url: "https://api.github.com/users/octocat",
                created_at: Instant.parse("2011-01-25T18:44:36Z")
        )

        when: "the mapper is called with null repos"
        def result = mapper.toUserRepoResponseData(null, user)

        then: "mapping succeeds and repos defaults to empty list"
        noExceptionThrown()
        result.getRepos() == []
        result.getUserName() == user.getLogin()
        result.getUrl() == user.getUrl()
    }
}
