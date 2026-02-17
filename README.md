# About this repo
## Contributors:
- Zachary Schuler (https://github.com/ZacharySchuler)
## Description: 
- The take home assignment for Branch made by Zachary Schuler. The repo is meant to represent a service used to fetch git data
## Api Contract:
- The api contract can be accessed via swagger at `localhost:8080/swagger-ui.html`
- There is also a postman collection in /postman
## Unit tests:
- The unit tests are written in groovy using the spock testing framework. 
- For those new to Spock, I find this to be a good intro to the syntax: https://spockframework.org/spock/docs/1.0/interaction_based_testing.html
## Data Sources:
- github
## Data Stores:
- No external Datastores/dbs are used
- There is an in-mem cache used (Hazelcast)

# Running the project
For first-time setup, use the steps below.

## Prerequisites
- Java 17 (project toolchain target is Java 17)

## Repo Location: 
- https://github.com/ZacharySchuler/branch-take-home.git


## Running the project:
- It is a standard springboot / gradle project. The application can be run using either:
  - An IDE java build configuration 
  - `./gradlew bootRun.` Note that run permissions will need to be given to ./gradlew (`CHMOD U+X ./gradlew`)


## Running tests (also generates JaCoCo coverage report)
- Tests can be run using `./gradlew test`
- Test report: `build/reports/tests/test/index.html`
- JaCoCo coverage report: `build/reports/jacoco/test/html/index.html`




# Design Decisions
## This repo was designed under the assumption that it would be expanded to support multiple git platforms (gitlab, bitbucket, etc.) and all types of git data
- Because of this assumption, Clients and Services were named to indicate that the current Impl was specific to Gitlab. 
- The endpoint GET (/api/v1/git-data/users/{user}/repos) was used because:
  - /api is a nice prefix in case  a host supports UI elements
  - v1 added because who doesn't love the ability to version
  - /git-data as the "service name" because i don't know if there are other services being hosted on the same host name. If not, this isn't nessiary since it can probably be derived from the host (www.branch-git-data/api/v1/...)
  - /users to signify that we're fetching USERS. All operations after this point would be the scope of the user service. Assuming there will be operations that aren't user based (even if this isn't true, I'd argue it should still be in the URI)
  - /{users} as a url param because it makes sense per REST. It also allows me to use HTTP404 for userNotFound (and have it not haunt me in my sleep)
  - /repos because we are specifically fetching repos. I'm assuming that not all operations in the user realm will involve repos.

## Response structure:
- The inclusion of the ResponseDto was an opinionated choice. While the prompt said to return a certain structure, it didn't say to return ONLY that data. ResponseDto.data is equal to the example JSON. Having a Data Transfer Object is a good practice in (especially java) services but the response could have just been the data elements. The reason why I opted to include the ResponseDto was the ability to return a definite status and an error object. As these were not part of the fundamental service domain model of a User's repos and loosely correspond to how the service represents data,  IMO it belongs in a DTO 
- Some may argue that HTTPCodes are enough to determine the state of the request and therefore the status field is unnessiary. I'm not a zealot about this and the normal HTTP codes can work for most use cases. I would argue that many times, using a lesser known status to describe the state of an operation e.g. using HTTP206 rather than status=Partial is a bastardization of the HTTPcodes (for the case of HTTP206, it should be used with a 'Range' header, which was not needed here). Ultimantly it's up to team/norms but as a current team of one, thats why I included it.
- I included the errors because error messages are very helpful. With Status and Error, I can be very descriptive for the Partial scenarios and hopefully return the data that MAY be needed even if a failure has occured.

## Logging:
- One might notice the UUID trace-Id. This is there to better track requests. It is meant to represent the entire lifecycle of a request. I generally like to accept it as an optional input for testing/ service-service communication sake but didn't bother with it for this. very helpful for log aggregators.
- I also like to add a Span-Id in addition to trace-id but again, felt overkill
- Consideration was given to using Info vs debug, vs error logging levels.
- If given more time, I'd probably set up some AOP type logging but felt like overkill for this

## Error Handling:
- Without knowing much about this service, I had to make some assumptions. I assumed that:
  1. It was useful to differentiate between an invalid user and a valid user + Github error -> introduced a custom exception to handle this to differentiate it from the feignExceptions
  2. If the user was valid and there was a different type of service error (4XX or 5XX), the end user did not care to know WHAT the actual issue was -> treating all thrown feignExceptions the same and therefore creating a custom userNotFound error
  3. That returning a Partial response was useful to the user in the case of where the user call passed but the repo call failed
- With beter understanding of these ^ assumptions, maybe a global exception handler would have been a different choice, but I doubt it because I have a general dislike of them (especially if there are many clients and the repo grows large)
- If assumption 3 is wrong, everything becomes a lot easier, but I felt it was a correct assumption, hence the error handling decisions because of it

## Caching:
- Without knowing much about the use case, I didn't spend too much time trying to optimize the caching.
- I went with a TTL of 10 min since the data COULD get stale. Again, I didn't know how big of a deal stale data was atm. If stale data was a big deal, we could differ the caching times between the repos and user data since the user data is less likely to change

## Libs Used:
- Java 17 and springboot 4.x.x just because JDK17 was the one installed on my system
- OpenApi because I am a BIG believer of code generated swaggers as documentation
- FeignClients because I think they are an easy client implementation. RestTemplate can also be used but feign clients are just so easy to spin up for no trade-off IMO
- Lombock to generate the Getts/Setters and Constructors. In easy CRUD apps like this, there is little trade off IMO and really reduce boiler-plate.
- Mapstruct for the model conversion. I don't always use Mapstruct but I find it clean and wanted to use it here.
- I know that Groovy and Spock are a very opinionated choice here but I enjoy using the Spock Framework. I find that for simple CRUD apps, Groovy's dynamically typed nature makes writing tests easier. I also really enjoy writing spock for its ease of testing interactions. Interaction testing is possible in Mockito but it is less readable IMO. After building retry functionality, interaction testing is a must IMO and spock is a more obvious choice


# If given more time, I would:
- clean up some of the app meta-data (name, version, etc)
- Add explicit dependency versions for build-consistency-sake
- Better configure Feign to handle things such as timeouts, conditional-retries, etc.
- Give some more time to think about caching strategies, being led by questions such as "Whats the volume", "how frequent", "whats the use case", etc.
- Generally clean up logging. Clean up the existing log statements to ensure they are properly scoped to a certain endpoint/function (and not generic and confusing) Probably add some AOP logging type things too
- Maybe add some testValidation as part of the build step (ensure testCoverage > approx 80% ). Plus properly exclude certain dirs (Application, config, client, etc.) from coverage consideration
- Better handle error responses with more unique/additional codes. Probably read them from a config file as well
- Spend a bit more time on the example objects for the swagger. Theres no easy + maintainable way to get specific example objects. Every way has their tradeoffs and felt doing so was out of scope.



# Note:
Note I commited as I went rather than just 1 big commit. I squashed it though as I only wished to be judged on the final result. The final result is free, but the git history costs extra :]
