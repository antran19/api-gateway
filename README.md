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

## Docker

`Dockerfile` copies a pre-built jar (`mvn clean package` first, then `docker build`) —
simpler than the old monorepo version, since there's no reactor to build from a root
context anymore. See the `infra` repo for the docker-compose setup that runs the whole
cluster.

## Follow-up (not done yet)

- Push a built image to a registry (e.g. GHCR) from CI, instead of building it fresh
  locally every time.
