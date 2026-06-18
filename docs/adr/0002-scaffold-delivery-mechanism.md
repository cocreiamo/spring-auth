# ADR-0002: Scaffold generator delivery mechanism

- **Status:** accepted
- **Date:** 2026-06-18
- **Deciders:** Francesco Bilotta

## Context

spring-auth has two halves, mirroring Laravel's Fortify + starter-kit split: an
auto-configuring starter (the headless engine, shipped as a Maven dependency) and a
**scaffold generator** that writes the auth screens (the seven auth views + the
account-settings views + the SPI wiring class) INTO the adopter's app, which the adopter
then owns and edits, exactly like Laravel's copied Blade views and action classes.

How the adopter invokes that generator is a one-way door for their muscle memory and for the
project's distribution surface: changing it later forces every adopter to relearn the command
and re-document their onboarding. The mechanism must be decided before Epic C (the generator)
starts. This ADR records the options; the decision is Francesco's.

## Decision

**(a) Maven build-plugin goal** (e.g. `mvn spring-auth:scaffold`). Lowest friction, no new
distribution channel to ship or sign, family-coherent (spring-gdpr ships a maven-plugin
module), and the adopter already has Maven. Epic C story C1 implements this one mechanism.
The standalone CLI (b) stays named as a later thin wrapper once there is adopter demand;
the initializr web generator (c) stays deferred.

## Options

### (a) Maven/Gradle build-plugin goal (e.g. `mvn spring-auth:scaffold`)
- **For:** stays in-build, no extra binary to install / ship / sign; the adopter already has
  Maven; family-coherent (spring-gdpr ships a maven-plugin module). The DESIGN doc leans here.
- **Against:** Gradle users need a separate plugin; goal invocation is clunkier than a CLI;
  generating source into `src/main` from a build plugin is slightly unusual.

### (b) Standalone CLI (e.g. `spring-auth init`)
- **For:** the best DX, closest to the `laravel new --livewire` feel; build-tool agnostic;
  room for an interactive prompt.
- **Against:** an extra binary to distribute, version, and sign; a new release channel
  (Homebrew / SDKMAN / native image) with permanent maintenance cost; the family currently
  ships nothing as a standalone binary.

### (c) Initializr-style web / template generator (start.spring.io feel)
- **For:** the most familiar on-ramp for Spring developers; zero local install; shareable URL.
- **Against:** by far the most infrastructure (a hosted service to run and secure); overkill
  for a single-org reference asset; couples adoption to an availability commitment.

## Consequences

The DESIGN doc's recommendation to consider (not a decision): start with the build-plugin
goal (lowest friction, no new distribution channel, family-coherent) and keep a thin CLI
wrapper as a later option once there is adopter demand. Whatever is chosen, Epic C story C1
implements exactly one mechanism first; the others stay deferred and named.

## Alternatives considered

See the three options above; this ADR exists precisely to hold them open until Francesco
decides, rather than letting Epic C silently pick one.
