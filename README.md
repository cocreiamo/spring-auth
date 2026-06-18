# spring-auth

> **Laravel's authentication DX, on Spring.** One starter auto-configures sane Spring Security defaults, ships the flows core Spring Security lacks (password reset, email verification, sudo window, login throttling), and a generator scaffolds the auth screens into your app. The `laravel new --livewire` feeling for the Spring world. Apache 2.0, no SaaS, no data egress.

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21%2B-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-4.0%2B-6db33f.svg)](https://spring.io/projects/spring-boot)

```
For:     A Spring Boot dev who wants Laravel-grade auth onboarding without hand-wiring
         Spring Security and reinventing password reset / email verification every project
Does:    Auto-configures form login + session + CSRF + DelegatingPasswordEncoder + logout,
         ships reset / verify / sudo-window / login-throttle flows over a user-provider SPI,
         and scaffolds the auth screens into your app. Sits ON TOP of Spring Security.
Effort:  add one starter, run one generator, adapt the user provider to your store
Cost:    Apache 2.0, no SaaS, no data egress
Status:  PREDISPOSED. The skeleton builds; the flows are tracked as issues (see Roadmap).
```

> **Status: predisposed, implementation tracked in issues.** This repository is the
> scaffolded skeleton of spring-auth: a building Maven project, the namespace and build
> harness, the design doc, and a structured issue backlog. The auth flows themselves are
> **not implemented yet**; each is a GitHub issue under one of the four epics below. The
> `SpringAuthAutoConfiguration` you see today is an empty placeholder that boots the context
> and pins the package and the master toggle, nothing more.

[**The idea**](#the-idea) ·
[**Architecture**](#architecture) ·
[**Quick start**](#quick-start-when-v1-ships) ·
[**Design doc**](docs/DESIGN.md) ·
[**ADRs**](docs/adr/) ·
[**Roadmap**](#roadmap)

---

## The idea

The `cocreiamo` mission is to bring Laravel's developer experience to the Spring world,
gradually. spring-auth is the first brick: it brings Laravel's **authentication** DX, the
`laravel new --livewire` starter-kit experience, to Spring.

In Laravel you run one command and get a working login / register / forgot / reset / verify /
logout flow with sane defaults, then you customise the views and point it at your user model.
On Spring you wire Spring Security by hand, and the flows Spring Security does not ship
(password reset, email verification, a sudo / password-confirm window, login throttling) get
rebuilt from scratch on every project. spring-auth closes that gap.

Crucially, **spring-auth sits ON TOP of Spring Security and never reimplements it.** It
auto-configures a `SecurityFilterChain` with sane defaults and adds the missing flows, all
overridable; you can always drop to raw `HttpSecurity`. Every bean is
`@ConditionalOnMissingBean`, and the auto-config backs off entirely when you declare your own
`SecurityFilterChain` (`@ConditionalOnMissingBean(SecurityFilterChain.class)`). The config is
a convenience over `HttpSecurity`, never a wall in front of it.

## Architecture: two halves (the Fortify + starter-kit split)

Laravel splits auth into **Fortify** (the headless engine: routes, login pipeline, password
broker, verification, no views) and the **starter kit** (the scaffold: views + overridable
action classes copied into your app). spring-auth mirrors that split exactly.

### (a) The auto-configuring starter (`spring-auth-spring-boot-starter`) -- the Fortify analog

A Maven dependency, headless, no views. An `@AutoConfiguration` that registers:

- **Sane Spring Security defaults**: form login, stateful session, CSRF on,
  `DelegatingPasswordEncoder` (`PasswordEncoderFactories.createDelegatingPasswordEncoder()`),
  session-fixation `changeSessionId()` (Laravel's regenerate-on-login, the Spring Security
  default), logout that invalidates the session and clears the cookie, opt-in remember-me.
- **The flows Spring Security lacks in core**, shipped as services + controllers behind
  interfaces: password reset (token store + signed token + mailer), email verification
  (signed link), password-confirm / sudo window, and a pluggable login rate limiter (the one
  place Laravel's Fortify ships more than the framework).
- Everything `@ConditionalOnMissingBean` + `@ConditionalOnProperty` (`spring-auth.features.*`)
  so any single piece is overridable and any flow is a toggle.

### (b) The scaffold generator -- the starter-kit analog

Writes the controllers / route config + templates for the seven auth screens + the
account-settings screens + the SPI wiring class INTO your `src/main`. You own and edit them
afterwards, exactly like Laravel's copied Blade views and action classes. Default render
target is plain Spring MVC + a template engine; the optional `--ui=lievit` set emits reactive
[lievit](https://github.com/lievit) components for the live-validation feel (a
template-selection seam, no compile dependency from the starter).

The delivery mechanism (build-plugin goal vs CLI vs initializr) and the default template
engine (Thymeleaf vs JTE) are **open decisions**, see [ADR-0002](docs/adr/0002-scaffold-delivery-mechanism.md) and [ADR-0003](docs/adr/0003-default-template-engine.md).

## Ships defaults, you adapt (the no-ORM principle)

This is the spring-gdpr philosophy, and it matters more here because **Java has no default
ActiveRecord**. Laravel leans on Eloquent; spring-auth must NOT assume an ORM. It exposes a
`UserDetailsService` / user-provider SPI plus overridable strategy seams (`UserRegistrar`,
`PasswordResetter`) with a sensible DEFAULT implementation, and every adopter adapts them to
their store, be it JDBC, JPA, or a legacy database. The library ships the orchestration and
the defaults; where your users actually live is the one thing only you know, so that stays a
small interface you implement.

```
spring-auth.provider = jdbc   -> a JDBC-backed UserDetailsService from a configured table
spring-auth.provider = custom -> your own UserDetailsService bean wins (@ConditionalOnMissingBean)
```

## Quick start (when v1 ships)

Distributed via [JitPack](https://jitpack.io), exactly like the rest of the cocreiamo /
iambilotta family. Maven Central is wired but **dormant** (see [ADR-0001](docs/adr/0001-namespace-and-distribution.md)).

**1. Add the JitPack repository:**

```xml
<repositories>
  <repository><id>jitpack.io</id><url>https://jitpack.io</url></repository>
</repositories>
```

**2. Add the starter** (JitPack derives the coordinate from the GitHub repo as
`com.github.cocreiamo.spring-auth`; the artifacts' own Maven groupId is `com.cocreiamo`):

```xml
<dependency>
  <groupId>com.github.cocreiamo.spring-auth</groupId>
  <artifactId>spring-auth-spring-boot-starter</artifactId>
  <version>v0.1.0</version>
</dependency>
```

> **A note on the groupId.** Consumers pulling from JitPack use
> `com.github.cocreiamo.spring-auth` (JitPack derives the groupId from the GitHub
> coordinates). The artifacts' own Maven coordinate is `com.cocreiamo` (the `groupId` in each
> module's `pom.xml`); that is what you use when you build and install locally. Same jars, two
> coordinate namespaces depending on how you resolve them.

**3. Adapt the user provider to your store, run the scaffold generator, customise the views.**
The full DX target and the SPI shape are in [`docs/DESIGN.md`](docs/DESIGN.md); the concrete
wiring lands as the issues below close.

## Roadmap

Implementation is tracked as a structured issue backlog on GitHub. Four epics, derived from
the [design doc](docs/DESIGN.md) section 3.6:

| Epic | Scope |
|---|---|
| **A -- Auto-config starter** | The headless engine: `SecurityFilterChain` defaults, `DelegatingPasswordEncoder`, user-provider SPI, `spring-auth.features.*` model, the escape-hatch back-off test |
| **B -- Flows Spring Security lacks** | Password reset, email verification, password-confirm sudo window, remember-me, login rate limiter |
| **C -- Scaffold generator** | The generator + delivery mechanism, the MVC template set, the lievit-backed set, account-settings scaffold, idempotent re-run |
| **D -- Project hygiene / docs** | README + quickstart, sample walking-skeleton app, CI gates, the deferred-features roadmap |

**Deferred (named, with the Laravel cognate):** API tokens (Sanctum / Passport), OAuth2 /
OIDC social login scaffold (Spring already ships `oauth2Login`), 2FA / TOTP (Fortify
`twoFactorAuthentication`), passkeys / WebAuthn (Spring ships `webAuthn`), multi-tenant.

## Module map

| Module | Purpose |
|---|---|
| `spring-auth-spring-boot-starter` | The auto-configuring starter (the Fortify analog). Today a building skeleton with a placeholder `@AutoConfiguration`. |

The scaffold-generator module is not created yet: its shape depends on the delivery-mechanism
decision ([ADR-0002](docs/adr/0002-scaffold-delivery-mechanism.md)). It is added under Epic C.

## About

Built by [Francesco Bilotta](https://iambilotta.com), Lead Software Engineer, under the
`cocreiamo` organisation. Sibling family assets:
[spring-gdpr](https://github.com/iambilotta/spring-gdpr) (GDPR evidence-as-code on Spring) and
[lievit](https://github.com/lievit) (Livewire-style reactive server-rendered components for
Spring). spring-auth is the first cocreiamo brick: Laravel's auth DX, brought to Spring.

Contact: francesco@iambilotta.com. Security reports: see [SECURITY.md](SECURITY.md).

## License

Apache License 2.0. See [LICENSE](LICENSE).
