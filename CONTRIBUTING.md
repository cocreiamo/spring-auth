# Contributing to spring-auth

Thanks for considering a contribution. The repo is predisposed (pre-1.0): the skeleton builds,
the flows are tracked as issues. The surface is small, the rules are short.

## What is in scope

- Implementing a backlog story (an epic / story from [`docs/DESIGN.md`](docs/DESIGN.md) section 3.6) under contract-first TDD.
- Bug fixes and production hardening on whatever is already implemented.
- New config / SPI surface ONLY when it serves the Laravel-DX mission and stays on top of Spring Security (never reimplements it).
- Documentation that adds an example or fills a real gap.
- Test coverage on production paths the existing suite leaves uncovered.

## What is out of scope

- A SaaS dashboard, web UI, or no-code admin panel.
- Wrappers for non-Spring stacks.
- Reimplementing a Spring Security mechanism that the framework already ships (the point is to configure it).
- Assuming an ORM (every store touch goes through an SPI with a default impl).
- A deferred feature ahead of v1: API tokens, OAuth2 social-login scaffold, 2FA / TOTP, passkeys, multi-tenant.

## Workflow

1. Open an issue first for anything beyond a typo or a one-line fix (or pick an existing backlog issue). We agree on shape before code.
2. Fork, branch, code, push.
3. PR against `main` with:
   - A description that says WHAT changed and WHY.
   - A test for the change. If a test is impossible, explain why in the PR.
   - All existing tests still green: `./mvnw -B clean verify`.

## Commit messages

Write commit messages future-you would want to read at 2am during an incident: a 50-72 char
imperative subject, a blank line, then a body explaining the WHY and the trade-off. Reference
the issue or PR. The existing commit log on `main` is the style guide.

## Coding style

- Java 21 baseline. Use records, sealed types, switch expressions where they help.
- No em dashes in comments or docs. Use commas, colons, periods, or `|`.
- Comments only when the WHY is non-obvious. Identifier names carry the WHAT.
- Verify Spring Security / Spring Boot 4 API names against the docs, never from memory.
- No new third-party runtime deps beyond Spring Security and Spring Boot without discussion.

## Tests

- Unit tests on starter logic, no Spring context where possible.
- `ApplicationContextRunner` for autoconfig wiring tests.
- `@SpringBootTest` + `spring-security-test` for full-stack auth flows where wiring matters.

## License + DCO

By submitting code, you agree to license it under Apache 2.0. Sign your commits with `-s` to
certify the [Developer Certificate of Origin](https://developercertificate.org). PRs without
DCO sign-off are not merged.

## Reporting security issues

Do NOT open a public issue for vulnerabilities. See [SECURITY.md](SECURITY.md).
