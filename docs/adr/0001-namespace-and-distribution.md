# ADR-0001: Namespace (com.cocreiamo) and distribution (JitPack, Central dormant)

- **Status:** accepted
- **Date:** 2026-06-18
- **Deciders:** Francesco Bilotta

## Context

spring-auth is the first brick of the `cocreiamo` organisation (mission: bring Laravel's
developer experience to Spring, gradually). The Java package and the Maven groupId are a
one-way door: renaming them after adopters depend on the coordinates is a breaking change.
The sibling family asset spring-gdpr uses package `com.iambilotta.gdpr.*`, groupId
`com.iambilotta.gdpr`, and is distributed via JitPack with Maven Central deliberately not
planned. The sibling OSS lievit uses `io.lievit.*`. The open question (flagged in
`docs/DESIGN.md` section 3.5 decision 1) was which root to adopt for the cocreiamo family.

Francesco owns the domain `cocreiamo.com`, so the reverse-DNS root `com.cocreiamo` is
verifiable and is the Central-namespace-eligible coordinate.

## Decision

- **Java package:** `com.cocreiamo.auth.*` (reverse-DNS of the owned domain cocreiamo.com).
- **Maven groupId:** `com.cocreiamo` in every module's pom. This is the domain-verified
  Maven Central namespace, kept DORMANT: the `release` profile wires GPG signing + the
  Central publishing plugin (`autoPublish=false`, `waitUntil=validated`) but nothing is
  published until a release is cut under that profile deliberately.
- **Live consumption path:** JitPack, exactly as the cocreiamo / iambilotta family does.
  JitPack derives the coordinate from the GitHub repository as `com.github.<org>.<repo>`
  and ignores the pom groupId for that coordinate, so adopters depend on
  `com.github.cocreiamo.spring-auth:spring-auth-spring-boot-starter`. The artifacts' own
  Maven coordinate is `com.cocreiamo` (used when building / installing locally). Same jars,
  two coordinate namespaces depending on how they are resolved.

## Consequences

- The package and groupId are now locked; changing either later is a breaking change for
  every adopter, so they will not change.
- Adopters pay zero ongoing maintenance cost: JitPack builds on demand from a tag, no GPG
  key custody, no Sonatype workflow, no immutable-release obligation.
- The Central namespace stays reserved and the pipeline stays wired, so promoting to Central
  later (if a real adopter requires it) is a profile flip plus a tag, not a re-architecture.
- The consume coordinate (`com.github.cocreiamo.spring-auth`) differs visibly from the pom
  groupId (`com.cocreiamo`); the README documents this split so it does not surprise anyone.

## Alternatives considered

- **`io.github.cocreiamo`** (the GitHub-pages-style Central namespace used by some OSS that
  do not own a domain). Rejected: Francesco owns cocreiamo.com, so the cleaner `com.cocreiamo`
  reverse-DNS is available and is the more authoritative coordinate.
- **A shared family root (e.g. one groupId across spring-auth + lievit + future bricks).**
  Rejected for now: lievit already shipped under `io.lievit.*` (its own one-way door, do not
  override), and forcing a shared root would either rename lievit or saddle spring-auth with
  lievit's namespace. Each brick owns its reverse-DNS; family coherence lives in the org and
  the docs, not in a forced shared groupId.
- **Publish to Maven Central from day one.** Rejected: same rationale as the rest of the
  family. The repo is a reference / portfolio asset; the permanent cost of a Central pipeline
  is only worth paying on concrete adopter demand. JitPack covers the consumer case today.
