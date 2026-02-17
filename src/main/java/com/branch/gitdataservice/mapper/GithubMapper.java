package com.branch.gitdataservice.mapper;

import com.branch.gitdataservice.model.clients.github.responses.GithubUserRepoResponseData;
import com.branch.gitdataservice.model.clients.github.responses.GithubUserResponseData;
import com.branch.gitdataservice.model.response.UserRepoResponseData;
import com.branch.gitdataservice.model.user.Repo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface GithubMapper {

    @Mapping(target = "userName", source = "user.login")
    @Mapping(target = "displayName", source = "user.name")
    @Mapping(target = "avatar", source = "user.avatar_url")
    @Mapping(target = "geoLocation", source = "user.location")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "url", source = "user.url")
    @Mapping(target = "createdAt", source = "user.created_at", qualifiedByName = "formatInstant")
    //TODO: Format this correctly
    @Mapping(target = "repos", source = "repos", qualifiedByName = "mapRepoToList")
    UserRepoResponseData toUserRepoResponseData(List<GithubUserRepoResponseData> repos, GithubUserResponseData user);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "url", source = "url")
    Repo toRepo(GithubUserRepoResponseData repo);

    @Named("mapRepoToList")
    default List<Repo> mapRepoToList(List<GithubUserRepoResponseData> repos) {
        if (repos == null) {
            return List.of();
        }

        List<Repo> repoList = new ArrayList<>();
        for (GithubUserRepoResponseData userRepo : repos) { //TODO: Research better namings
            if (userRepo != null) {
                repoList.add(new Repo(userRepo.getName(), userRepo.getUrl()));
            }

        }
        return repoList;
    }

    @Named("formatInstant")
    default String formatInstantToRFC_1123(Instant createdAt) {
        try {
            if (createdAt == null) {
                return null;
            }
            return DateTimeFormatter.RFC_1123_DATE_TIME //e.g. Tue, 25 Jan 2011 18:44:36 GMT
                    .withLocale(Locale.ENGLISH)
                    .withZone(ZoneOffset.UTC)
                    .format(createdAt);
        } catch (Exception e) {
            return null;
        }

    }
}
