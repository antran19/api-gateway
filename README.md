# api-gateway

Spring Cloud Gateway — single entry point for Project Nexus's microservices. Coarse JWT
validation (signature/expiry) and method-aware routing (GET public, other methods
require auth) happen here before requests reach a downstream service. Extracted from the
original monorepo into its own standalone project, per the course's polyrepo requirement
(each microservice its own repo, its own independent Spring Boot project).

## Prerequisites

This project resolves `com.nexus:common-security` (version `1.0.0`) from
[antran19/common-libs](https://github.com/antran19/common-libs)'s GitHub Packages
registry, declared in `pom.xml`'s `<repositories>` block. Reading GitHub Packages requires
authentication even for a public repo; add a GitHub Personal Access Token
(`read:packages` scope) to your own `~/.m2/settings.xml` once:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

(Alternative for local dev without a token: clone `common-libs` and run
`mvn clean install` there; Maven checks your local `~/.m2` cache before reaching out to
GitHub Packages, so that works too.)

## Build & test

```bash
mvn clean package
```

## Run

```bash
java -jar target/api-gateway-0.1.0-SNAPSHOT.jar
```

Expects a running `discovery-server` to register with and discover downstream services
through (`eureka.client.serviceUrl.defaultZone` in `src/main/resources/application.yml`).
Routes are also configured there by service name (Eureka-resolved), so `discovery-server`
must be up first, and downstream services must be registered before requests can route to
them.

## Follow-up (not done yet)

- Set up this repo's own CI/CD pipeline (build, test, Docker image, push to a registry) —
  if it runs `mvn`, give the workflow `permissions: packages: read` and wire up
  `server-id: github` in its `actions/setup-java` step so it can resolve `common-security`
  from GitHub Packages, same as `common-libs`' own `publish.yml` does for publishing.
- `Dockerfile` in this repo can be simplified since this is no longer a multi-module
  reactor — a plain single-project Docker build context now works.
