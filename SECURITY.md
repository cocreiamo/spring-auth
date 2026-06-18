# Security Policy

`spring-auth` is an authentication library: security reports are taken seriously.

## Supported versions

`spring-auth` is pre-1.0 (predisposed). Once releases exist, only the latest minor receives
security fixes during the 0.x series. After v1.0, the support window will be the latest two
minor versions.

## Reporting a vulnerability

Do **NOT** open a public GitHub issue for vulnerabilities.

Send a private report to `francesco@iambilotta.com` (or open a private security advisory on
the repo) with:

- A short description of the issue and the affected component (auto-config, a flow such as
  password reset / email verification / sudo window / login throttle, an SPI default, the
  scaffold output).
- The version of `spring-auth` and the runtime stack (JDK, Spring Boot, Spring Security).
- Reproduction steps or a minimal code sample.
- Whether the vulnerability is already public elsewhere.

## Process

- Acknowledgement within 5 business days.
- Triage and severity assessment within 10 business days.
- Coordinated disclosure: a 90-day embargo from acknowledgement to public advisory, unless an
  active exploit is observed, in which case the timeline shortens.
- A patched release, a CVE if warranted, and a credit line for the reporter (or anonymous, on
  request).

## Out of scope

- Misconfigurations downstream of the library (weak password policy left at defaults, an
  adopter SecurityFilterChain that overrides the spring-auth defaults insecurely, missing TLS,
  a user-provider SPI that trusts unvalidated input). These are user responsibility; the
  README and the scaffold show the right pattern.
- Vulnerabilities in transitive dependencies (Spring Security itself). Report those upstream;
  we track and bump via Dependabot.
- Theoretical issues without a reproduction.
