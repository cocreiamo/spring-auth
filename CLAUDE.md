# CLAUDE.md -- `spring-auth` (repo working contract)

The technical working contract for this repo. The README is the **usage + pitch** surface;
`docs/DESIGN.md` is the **v1 design + backlog source of truth**; the ADRs are the
**decisions**; the tests are the **as-built reality**. This file says how to work here
without duplicating any of them.

## What this is

Apache-2.0 Spring Boot library: Laravel's authentication DX brought to Spring. The first
brick of the `cocreiamo` org (Laravel DX -> Spring, gradually). Two halves, mirroring
Laravel's Fortify + starter-kit split: an `@AutoConfiguration` **starter** that registers
sane Spring Security defaults plus the flows Spring Security lacks in core (password reset,
email verification, password-confirm sudo window, login throttling) over a user-provider SPI,
and a **scaffold generator** that writes the auth screens into the adopter's app. JitPack-
distributed, Maven Central wired but dormant (ADR-0001). A reference / portfolio asset.

**Status: predisposed.** The skeleton builds; the flows are NOT implemented. Each flow is a
GitHub issue under one of four epics (A auto-config, B missing flows, C scaffold, D hygiene).
`SpringAuthAutoConfiguration` is currently an empty placeholder.

## The non-negotiable principle

spring-auth sits ON TOP of Spring Security and **never reimplements it**. It auto-configures
a `SecurityFilterChain` and adds missing flows, all `@ConditionalOnMissingBean` /
`@ConditionalOnProperty`, and backs off entirely when the adopter declares their own
`SecurityFilterChain`. If a change would have spring-auth replace a Spring Security mechanism
rather than configure it, the change is wrong-shaped.

## Ships defaults, you adapt (no ORM assumption)

Java has no default ActiveRecord. spring-auth must NOT assume an ORM. Expose a
`UserDetailsService` / user-provider SPI plus overridable seams (`UserRegistrar`,
`PasswordResetter`) with a sensible DEFAULT impl; the adopter adapts to their store (JDBC /
JPA / legacy). The library ships orchestration + defaults, never the adopter's persistence.

## The canon governance (where each kind of fact lives)

The strongest-deterministic layer that can hold a rule owns it; prose is the last resort.

| Artifact | Holds | Discipline |
|---|---|---|
| `docs/DESIGN.md` | the v1 design + scope + the 4-epic / ~18-story backlog | the source of truth for repo content; revise as decisions land |
| `docs/adr/*.md` | locked **design decisions** (MADR-lite) | never delete; supersede with a new ADR and cross-link |
| tests (`@Test`) | the **as-built reality** (each test is a requirement) | every test pins one property |
| code comments | non-obvious **invariants** that survive refactors | no session chronicle, no decision narrative |
| `CHANGELOG.md` | the released delta per version | Keep-a-Changelog shape |

## Hard rules

- **No production code without a test that demands it.** Unit tests on starter logic boot no
  Spring context where possible; `ApplicationContextRunner` for autoconfig wiring;
  `@SpringBootTest` / `spring-security-test` for full-stack auth where the wiring matters.
- **On top of Spring Security, never instead of it** (see the principle above).
- **Ships defaults, you adapt**: never assume an ORM; every store touch is behind an SPI with
  a default impl and a `@ConditionalOnMissingBean` override.
- **Verify Spring Security / Spring Boot 4 API names against the docs**, never from memory
  (context7 or the spring-gdpr code as a known-good reference). The doc-first rule is sharper
  here because the surface is Spring Security 7.0 / Spring Boot 4.0.
- **English** for code, comments, commits, ADRs. **No em-dashes.**
- **No breaking change to the public SPI / config surface** without an ADR and a deprecation
  cycle.
- Namespace is locked (ADR-0001): package `com.cocreiamo.auth.*`, groupId `com.cocreiamo`,
  JitPack consume `com.github.cocreiamo.spring-auth`. One-way door, do not deviate.

## Build entry points

```
./mvnw -B clean verify        # full gate: unit + autoconfig wiring + (later) Spotless
./mvnw -q -DskipTests package # quick compile sanity
```

Java 21 baseline (compiler `release=21`), Spring Boot 4.0.6. Builds on a Java 25 JDK too
(the toolchain targets 21).

## Adding an ADR

Copy `docs/adr/0000-template.md` to the next number, kebab-case the title, status `proposed`,
flip to `accepted` after review. Never delete an old ADR; mark `superseded` / `deprecated` and
link both ways. Two decisions are deliberately OPEN (ADR-0002 scaffold delivery, ADR-0003
template engine): resolve them with Francesco before the epic that needs them starts.

## Scope discipline

Implement epic by epic, story by story, from `docs/DESIGN.md` section 3.6. The deferred
features (API tokens, OAuth2 social scaffold, 2FA, passkeys, multi-tenant) are named on
purpose; do not half-build them. v1 is the login / register / forgot / reset / verify / logout
walking skeleton with the SPI and the scaffold.
